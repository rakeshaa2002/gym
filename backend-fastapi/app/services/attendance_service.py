import logging
from datetime import date, datetime, time

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.exceptions import ForbiddenException
from app.models.attendance import Attendance
from app.models.fitness_user import FitnessUser
from app.models.membership import MembershipPlan
from app.models.role import Role
from app.models.users import Users
from app.schemas.attendance import AccessDecisionResponse, AttendanceMemberResponse, AttendanceResponse
from app.services import notification_service

log = logging.getLogger("fitnexus.attendance")

_STAFF_ROLES = {Role.SUPER_ADMIN, Role.ADMIN, Role.MANAGER, Role.TRAINER}


def is_staff(role) -> bool:
    return Role(role) in _STAFF_ROLES


def _require_staff(user: Users) -> None:
    if not is_staff(user.role):
        raise ForbiddenException("Only staff can perform this action")


def _to_response(a: Attendance, member: Users | None) -> AttendanceResponse:
    return AttendanceResponse(
        id=a.id, memberId=a.user_id, memberName=member.name if member else None,
        memberEmail=member.email if member else None, attendanceDate=a.attendance_date.isoformat(),
        checkInTime=a.check_in_time.isoformat(), checkOutTime=a.check_out_time.isoformat() if a.check_out_time else None,
        method=a.method, deviceId=a.device_id, status=a.status,
    )


def _find_open_session(db: Session, user_id: int) -> Attendance | None:
    return db.scalar(
        select(Attendance)
        .where(Attendance.user_id == user_id, Attendance.check_out_time.is_(None))
        .order_by(Attendance.check_in_time.desc())
        .limit(1)
    )


def _check_access(db: Session, member: Users, now: time) -> tuple[str | None, bool]:
    """Returns (denial_message, missing_slot)."""
    if Role(member.role) != Role.USER:
        return None, False

    fu = db.get(FitnessUser, member.id)
    plan = db.get(MembershipPlan, fu.membership_plan_id) if fu and fu.membership_plan_id else None
    unlimited = bool(plan.unlimited_access) if plan else (fu and (fu.membership_plan or "").upper() == "PREMIUM")
    if unlimited:
        return None, False

    if fu is None or fu.access_start_time is None or fu.access_end_time is None:
        return (
            "No check-in time slot is assigned to you yet. Please contact your admin to set up your slot timing.",
            True,
        )

    grace = settings.attendance_access_grace_minutes
    start_minutes = fu.access_start_time.hour * 60 + fu.access_start_time.minute - grace
    end_minutes = fu.access_end_time.hour * 60 + fu.access_end_time.minute + grace
    now_minutes = now.hour * 60 + now.minute
    if start_minutes <= now_minutes <= end_minutes:
        return None, False
    return (
        f"Wrong check-in time. Your slot is {fu.access_start_time.strftime('%H:%M')}–"
        f"{fu.access_end_time.strftime('%H:%M')}. Please come during your slot, or contact your admin to change it.",
        False,
    )


def _notify_staff_need_slot(db: Session, member: Users) -> None:
    fu = db.get(FitnessUser, member.id)
    if fu and fu.last_slot_request_at and (datetime.now() - fu.last_slot_request_at).total_seconds() < 6 * 3600:
        return
    recipients = {uid for uid in (member.admin_id, member.manager_id) if uid}
    if fu and fu.assigned_trainer_id:
        recipients.add(fu.assigned_trainer_id)
    if not recipients:
        recipients = set(db.scalars(select(Users.id).where(Users.role == Role.ADMIN)))
    for rid in recipients:
        notification_service.create_for_user(
            db, rid, "INFO", "Check-in slot needed", f"{member.name} needs a check-in time slot assigned.", "/attendance",
        )
    if fu:
        fu.last_slot_request_at = datetime.now()
        db.commit()


def _notify_trainer_of_checkin(db: Session, member: Users) -> None:
    try:
        fu = db.get(FitnessUser, member.id)
        trainer_account_id = fu.assigned_trainer_id if fu else None
        trainer_account_id = trainer_account_id or member.trainer_id
        if trainer_account_id:
            notification_service.create_for_user(
                db, trainer_account_id, "INFO", "Member checked in",
                f"{member.name} checked in at {datetime.now().strftime('%H:%M')}.", "/attendance",
            )
    except Exception:
        log.warning("notify_trainer_of_checkin failed", exc_info=True)


def _record_scan(db: Session, member: Users | None, method: str, device_id: str | None) -> AccessDecisionResponse:
    now = datetime.now()
    if member is None:
        return AccessDecisionResponse(
            accessGranted=False, openGate=False, action="DENIED", memberId=None, memberName=None,
            time=now.isoformat(), message="Member not recognized",
        )
    if not member.is_active:
        return AccessDecisionResponse(
            accessGranted=False, openGate=False, action="DENIED", memberId=member.id, memberName=member.name,
            time=now.isoformat(), message="Membership is inactive",
        )

    open_session = _find_open_session(db, member.id)
    if open_session is not None:
        open_session.check_out_time = now
        open_session.status = "CHECKED_OUT"
        open_session.updated_at = now
        db.commit()
        return AccessDecisionResponse(
            accessGranted=True, openGate=True, action="CHECK_OUT", memberId=member.id, memberName=member.name,
            time=now.isoformat(), message="Checked out",
        )

    denial, missing_slot = _check_access(db, member, now.time())
    if denial:
        if missing_slot:
            _notify_staff_need_slot(db, member)
        return AccessDecisionResponse(
            accessGranted=False, openGate=False, action="DENIED", memberId=member.id, memberName=member.name,
            time=now.isoformat(), message=denial,
        )

    record = Attendance(
        user_id=member.id, attendance_date=now.date(), check_in_time=now, method=method, device_id=device_id,
        status="CHECKED_IN",
    )
    db.add(record)
    db.commit()
    _notify_trainer_of_checkin(db, member)
    return AccessDecisionResponse(
        accessGranted=True, openGate=True, action="CHECK_IN", memberId=member.id, memberName=member.name,
        time=now.isoformat(), message="Checked in",
    )


def _resolve_by_fingerprint_userid_email(
    db: Session, fingerprint_id: str | None, user_id: int | None, email: str | None
) -> Users | None:
    if fingerprint_id:
        member = db.scalar(select(Users).where(Users.fingerprint_id == fingerprint_id))
        if member:
            return member
    if user_id:
        member = db.get(Users, user_id)
        if member:
            return member
    if email:
        return db.scalar(select(Users).where(Users.email == email))
    return None


def device_check_in(db: Session, fingerprint_id, user_id, email, device_id, provided_key: str | None) -> AccessDecisionResponse:
    device_key = settings.attendance_device_key
    if device_key and device_key.strip() and device_key != provided_key:
        raise ForbiddenException("Invalid device key")
    member = _resolve_by_fingerprint_userid_email(db, fingerprint_id, user_id, email)
    return _record_scan(db, member, "FINGERPRINT", device_id)


def _resolve_kiosk_identifier(db: Session, identifier: str) -> Users | None:
    identifier = (identifier or "").strip()
    if not identifier:
        return None
    member = db.scalar(select(Users).where(Users.fingerprint_id == identifier))
    if member:
        return member
    if identifier.upper().startswith("FNX"):
        digits = "".join(c for c in identifier if c.isdigit())
        if digits:
            member = db.get(Users, int(digits))
            if member:
                return member
    if identifier.isdigit():
        member = db.get(Users, int(identifier))
        if member:
            return member
    return db.scalar(select(Users).where(Users.email == identifier))


def kiosk_check_in(db: Session, identifier: str, staff: Users) -> AccessDecisionResponse:
    _require_staff(staff)
    member = _resolve_kiosk_identifier(db, identifier)
    return _record_scan(db, member, "KIOSK", None)


def self_check_in(db: Session, user: Users) -> AccessDecisionResponse:
    return _record_scan(db, user, "APP", None)


def manual_check_in(db: Session, member_id: int, staff: Users) -> AccessDecisionResponse:
    _require_staff(staff)
    member = db.get(Users, member_id)
    return _record_scan(db, member, "MANUAL", None)


def enroll_fingerprint(db: Session, member_id: int, fingerprint_id: str, staff: Users) -> None:
    _require_staff(staff)
    fingerprint_id = (fingerprint_id or "").strip()
    if not fingerprint_id:
        raise ValueError("fingerprintId is required")
    dup = db.scalar(select(Users).where(Users.fingerprint_id == fingerprint_id, Users.id != member_id))
    if dup is not None:
        raise ValueError("This fingerprint is already enrolled to another member")
    member = db.get(Users, member_id)
    if member is None:
        raise ValueError("Member not found")
    member.fingerprint_id = fingerprint_id
    db.commit()


def get_my_attendance(db: Session, user: Users) -> list[AttendanceResponse]:
    rows = db.scalars(select(Attendance).where(Attendance.user_id == user.id).order_by(Attendance.check_in_time.desc()))
    return [_to_response(a, user) for a in rows]


def get_enrollable_members(db: Session, staff: Users) -> list[AttendanceMemberResponse]:
    _require_staff(staff)
    rows = db.scalars(select(Users).where(Users.role == Role.USER, Users.is_active.is_(True)))
    members = sorted(rows, key=lambda u: (u.name or "").lower())
    return [AttendanceMemberResponse(id=u.id, name=u.name, email=u.email) for u in members]


def get_all(db: Session, staff: Users, on_date: date | None) -> list[AttendanceResponse]:
    _require_staff(staff)
    stmt = select(Attendance).order_by(Attendance.check_in_time.desc())
    if on_date is not None:
        stmt = stmt.where(Attendance.attendance_date == on_date)
    rows = list(db.scalars(stmt))
    members = {u.id: u for u in db.scalars(select(Users).where(Users.id.in_({a.user_id for a in rows})))} if rows else {}
    return [_to_response(a, members.get(a.user_id)) for a in rows]


def get_today(db: Session, staff: Users) -> list[AttendanceResponse]:
    return get_all(db, staff, date.today())
