import logging
import time as time_module
from datetime import date, datetime, time
from datetime import timedelta

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.exceptions import EntityNotFoundException, ForbiddenException, InvalidOperationException
from app.models.fitness_user import FitnessUser
from app.models.membership import MembershipPlan, MembershipRequest, Transaction
from app.models.role import Role
from app.models.users import Users
from app.schemas.membership import (
    AssignMembershipRequest,
    CreateOrderRequest,
    MembershipRequestResponse,
    MembershipResponse,
    OrderResponse,
    UpgradeMembershipRequest,
    VerifyPaymentRequest,
)
from app.services import razorpay_service
from app.core.config import settings

log = logging.getLogger("fitnexus.membership")

_STAFF_ROLES = {Role.SUPER_ADMIN, Role.ADMIN, Role.MANAGER, Role.TRAINER}

_DISCOUNTS = {3: 0.10, 12: 0.20}


def is_staff(role) -> bool:
    return Role(role) in _STAFF_ROLES


def _get_or_create_fitness_user(db: Session, account: Users) -> FitnessUser:
    fu = db.get(FitnessUser, account.id)
    if fu is None:
        fu = FitnessUser(id=account.id, details_completed=False, registration_date=datetime.now())
        fu.account = account
        db.add(fu)
        db.flush()
    return fu


def _resolve_plan_ref(db: Session, fu: FitnessUser) -> MembershipPlan | None:
    if fu.membership_plan_id:
        return db.get(MembershipPlan, fu.membership_plan_id)
    return None


def _is_unlimited(plan: MembershipPlan | None, legacy_code: str | None) -> bool:
    if plan is not None:
        return bool(plan.unlimited_access)
    return (legacy_code or "").upper() == "PREMIUM"


def build_response(db: Session, me: Users) -> MembershipResponse:
    if Role(me.role) != Role.USER:
        return MembershipResponse(
            plan="STAFF", expiry=None, daysLeft=None, premium=False, applicable=False, planId=None,
            planName=None, unlimitedAccess=False, accessStartTime=None, accessEndTime=None, maxSessionMinutes=0,
        )

    fu = db.get(FitnessUser, me.id)
    plan_code = fu.membership_plan if fu else "BASIC"
    expiry = fu.membership_expiry if fu else None
    plan_ref = _resolve_plan_ref(db, fu) if fu else None
    unlimited = _is_unlimited(plan_ref, plan_code)
    active = expiry is None or expiry >= date.today()
    days_left = (expiry - date.today()).days if (active and expiry) else None
    premium = active and (unlimited or (plan_code or "").upper() == "PREMIUM")

    return MembershipResponse(
        plan=plan_code or "BASIC", expiry=expiry.isoformat() if expiry else None, daysLeft=days_left,
        premium=premium, applicable=True, planId=plan_ref.id if plan_ref else None,
        planName=plan_ref.name if plan_ref else None, unlimitedAccess=unlimited,
        accessStartTime=fu.access_start_time.strftime("%H:%M") if fu and fu.access_start_time else None,
        accessEndTime=fu.access_end_time.strftime("%H:%M") if fu and fu.access_end_time else None,
        maxSessionMinutes=(plan_ref.max_session_minutes or 0) if plan_ref else 0,
    )


def get_member_membership(db: Session, member_id: int, requester: Users) -> MembershipResponse:
    if requester.id != member_id and not is_staff(requester.role):
        raise ForbiddenException("Not permitted to view this member's membership")
    account = db.get(Users, member_id)
    if account is None:
        raise EntityNotFoundException("Member not found")
    return build_response(db, account)


def _parse_hhmm(value: str | None) -> time | None:
    if not value:
        return None
    h, m = value.split(":")
    return time(int(h), int(m))


def assign_membership(db: Session, member_id: int, request: AssignMembershipRequest, staff: Users) -> MembershipResponse:
    if not is_staff(staff.role):
        raise ForbiddenException("Only staff can assign memberships")
    account = db.get(Users, member_id)
    if account is None:
        raise EntityNotFoundException("Member not found")
    plan = db.get(MembershipPlan, request.planId)
    if plan is None:
        raise EntityNotFoundException("Membership plan not found")

    fu = _get_or_create_fitness_user(db, account)

    if not plan.unlimited_access:
        if not request.accessStartTime or not request.accessEndTime:
            raise ValueError("accessStartTime and accessEndTime are required for a non-unlimited plan")
        start, end = _parse_hhmm(request.accessStartTime), _parse_hhmm(request.accessEndTime)
        if end <= start:
            raise ValueError("accessEndTime must be after accessStartTime")
        fu.access_start_time, fu.access_end_time = start, end
    else:
        fu.access_start_time, fu.access_end_time = None, None

    credit_days = _upgrade_credit_days(db, fu, plan)
    months = request.months if request.months and request.months > 0 else 1

    if plan.duration_days > 0:
        fu.membership_expiry = date.today() + timedelta(days=plan.duration_days * months + credit_days)
    else:
        fu.membership_expiry = None

    fu.membership_plan = plan.code
    fu.membership_plan_id = plan.id

    db.add(
        Transaction(
            member_id=member_id, plan_id=plan.id, amount=float(plan.price * months), months=months,
            payment_method="CASH", status="SUCCESS", transaction_date=datetime.now(),
        )
    )

    pending = db.scalar(
        select(MembershipRequest).where(MembershipRequest.member_id == member_id, MembershipRequest.status == "PENDING")
    )
    if pending is not None:
        pending.status = "RESOLVED"
        pending.resolved_at = datetime.now()

    db.commit()
    return build_response(db, account)


def _daily_rate(plan: MembershipPlan) -> float:
    duration = plan.duration_days if plan.duration_days > 0 else 30
    return plan.price / duration


def _upgrade_credit_rupees(db: Session, fu: FitnessUser | None, new_plan: MembershipPlan) -> float:
    if fu is None or not fu.membership_plan_id or fu.membership_plan_id == new_plan.id:
        return 0.0
    current_plan = db.get(MembershipPlan, fu.membership_plan_id)
    if current_plan is None or current_plan.price <= 0:
        return 0.0
    if fu.membership_expiry is None or fu.membership_expiry < date.today():
        return 0.0
    days_left = (fu.membership_expiry - date.today()).days
    return round(days_left * _daily_rate(current_plan))


def _upgrade_credit_days(db: Session, fu: FitnessUser | None, new_plan: MembershipPlan) -> int:
    credit_rupees = _upgrade_credit_rupees(db, fu, new_plan)
    if credit_rupees <= 0:
        return 0
    return int(round(credit_rupees / _daily_rate(new_plan)))


def _price_for_period(plan: MembershipPlan, months: int) -> int:
    discount = _DISCOUNTS.get(months, 0.0)
    return round(plan.price * months * (1 - discount))


def _normalize_months(months: int | None) -> int:
    return months if months and months > 0 else 1


def _activate_plan(
    db: Session, me: Users, plan: MembershipPlan, months: int, method: str,
    rzp_order_id: str | None, rzp_payment_id: str | None,
) -> MembershipResponse:
    fu = _get_or_create_fitness_user(db, me)
    same_plan = fu.membership_plan_id == plan.id
    today = date.today()

    if same_plan and fu.membership_expiry and fu.membership_expiry >= today:
        base = fu.membership_expiry
    else:
        base = today
    fu.membership_expiry = base + timedelta(days=plan.duration_days * months) if plan.duration_days > 0 else None

    if plan.unlimited_access:
        fu.access_start_time, fu.access_end_time = None, None

    fu.membership_plan = plan.code
    fu.membership_plan_id = plan.id

    db.add(
        Transaction(
            member_id=me.id, plan_id=plan.id, amount=float(_price_for_period(plan, months)), months=months,
            payment_method=method, status="SUCCESS", transaction_date=datetime.now(),
            razorpay_order_id=rzp_order_id, razorpay_payment_id=rzp_payment_id,
        )
    )
    db.commit()
    return build_response(db, me)


def _resolve_plan_by_code(db: Session, code: str | None) -> MembershipPlan:
    code = (code or "PREMIUM").strip().upper()
    plan = db.scalar(select(MembershipPlan).where(MembershipPlan.code == code))
    if plan is None:
        raise EntityNotFoundException(f"Membership plan '{code}' not found")
    return plan


def upgrade(db: Session, request: UpgradeMembershipRequest, me: Users) -> MembershipResponse:
    if Role(me.role) != Role.USER:
        raise ForbiddenException("Only members can upgrade their own membership")
    plan = _resolve_plan_by_code(db, request.plan)
    months = _normalize_months(request.months)
    return _activate_plan(db, me, plan, months, "MOCK_UPGRADE", None, None)


def request_plan_change(db: Session, request, me: Users) -> None:
    if Role(me.role) != Role.USER:
        raise ForbiddenException("Only members can request a plan change")
    existing = db.scalar(
        select(MembershipRequest).where(MembershipRequest.member_id == me.id, MembershipRequest.status == "PENDING")
    )
    if existing is not None:
        existing.requested_plan_id = request.planId
        existing.note = request.note
    else:
        db.add(MembershipRequest(member_id=me.id, requested_plan_id=request.planId, note=request.note, status="PENDING"))
    db.commit()


def get_pending_requests(db: Session, staff: Users) -> list[MembershipRequestResponse]:
    if not is_staff(staff.role):
        raise ForbiddenException("Only staff can view membership requests")
    rows = db.scalars(
        select(MembershipRequest).where(MembershipRequest.status == "PENDING").order_by(MembershipRequest.created_at.desc())
    )
    result = []
    for r in rows:
        member = db.get(Users, r.member_id)
        fu = db.get(FitnessUser, r.member_id)
        plan = db.get(MembershipPlan, r.requested_plan_id) if r.requested_plan_id else None
        result.append(
            MembershipRequestResponse(
                id=r.id, memberId=r.member_id, memberName=member.name if member else "",
                memberEmail=member.email if member else "", currentPlan=fu.membership_plan if fu else "BASIC",
                requestedPlanId=r.requested_plan_id, requestedPlan=plan.name if plan else None,
                note=r.note, createdAt=r.created_at.isoformat(),
            )
        )
    return result


def dismiss_request(db: Session, request_id: int, staff: Users) -> None:
    if not is_staff(staff.role):
        raise ForbiddenException("Only staff can dismiss membership requests")
    row = db.get(MembershipRequest, request_id)
    if row is None:
        raise EntityNotFoundException("Membership request not found")
    row.status = "RESOLVED"
    row.resolved_at = datetime.now()
    db.commit()


def create_order(db: Session, request: CreateOrderRequest, me: Users) -> OrderResponse:
    if Role(me.role) != Role.USER:
        raise ForbiddenException("Only members can purchase a membership")
    plan = db.get(MembershipPlan, request.planId)
    if plan is None:
        raise EntityNotFoundException("Membership plan not found")
    if plan.price <= 0:
        raise ValueError("This plan cannot be purchased")
    if not razorpay_service.is_configured():
        raise InvalidOperationException("Razorpay is not configured on the server")

    months = _normalize_months(request.months)
    full_price = _price_for_period(plan, months)
    fu = db.get(FitnessUser, me.id)
    credit = _upgrade_credit_rupees(db, fu, plan)
    payable = max(0, full_price - credit)
    amount_paise = max(int(payable), 1) * 100

    receipt = f"mem_{me.id}_{int(time_module.time() * 1000)}"
    order_id = razorpay_service.create_order(amount_paise, "INR", receipt)

    return OrderResponse(
        orderId=order_id, keyId=settings.razorpay_key_id, amount=amount_paise, currency="INR", months=months,
        name="FitNexus Gym", description=f"{plan.name} membership ({months} month{'s' if months > 1 else ''})",
        originalAmount=full_price * 100, creditApplied=int(credit) * 100,
    )


def verify_and_activate(db: Session, request: VerifyPaymentRequest, me: Users) -> MembershipResponse:
    if Role(me.role) != Role.USER:
        raise ForbiddenException("Only members can verify a payment")
    if request is None or not razorpay_service.verify_signature(
        request.razorpayOrderId, request.razorpayPaymentId, request.razorpaySignature
    ):
        raise InvalidOperationException("Payment verification failed")
    plan = db.get(MembershipPlan, request.planId) if request.planId else _resolve_plan_by_code(db, "PREMIUM")
    if plan is None:
        raise EntityNotFoundException("Membership plan not found")
    months = _normalize_months(request.months)
    return _activate_plan(db, me, plan, months, "RAZORPAY", request.razorpayOrderId, request.razorpayPaymentId)


def cancel(db: Session, me: Users) -> MembershipResponse:
    if Role(me.role) != Role.USER:
        raise ForbiddenException("Only members can cancel their own membership")
    fu = _get_or_create_fitness_user(db, me)
    basic = db.scalar(select(MembershipPlan).where(MembershipPlan.code == "BASIC"))
    fu.membership_plan = "BASIC"
    fu.membership_plan_id = basic.id if basic else None
    fu.membership_expiry = None
    db.commit()
    return build_response(db, me)
