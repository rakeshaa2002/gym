from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.exceptions import InvalidOperationException
from app.models.membership import MembershipPlan
from app.schemas.membership import MembershipPlanRequest, MembershipPlanResponse


def _to_response(p: MembershipPlan) -> MembershipPlanResponse:
    return MembershipPlanResponse(
        id=p.id, code=p.code, name=p.name, description=p.description, price=p.price,
        durationDays=p.duration_days, maxSessionMinutes=p.max_session_minutes,
        unlimitedAccess=p.unlimited_access, trainerChat=bool(p.trainer_chat),
        features=[f for f in (p.features or "").split("\n") if f.strip()],
        active=p.active, updatedAt=p.updated_at.isoformat(),
    )


def get_all(db: Session, active_only: bool) -> list[MembershipPlanResponse]:
    stmt = select(MembershipPlan)
    if active_only:
        stmt = stmt.where(MembershipPlan.active.is_(True))
    plans = sorted(db.scalars(stmt), key=lambda p: p.price)
    return [_to_response(p) for p in plans]


def get_by_id(db: Session, id_: int) -> MembershipPlanResponse:
    plan = db.get(MembershipPlan, id_)
    if plan is None:
        raise InvalidOperationException(f"Membership plan {id_} not found")
    return _to_response(plan)


def _normalize_code(code: str) -> str:
    code = (code or "").strip().upper()
    if not code:
        raise ValueError("Plan code is required")
    return "_".join(code.split())


def create(db: Session, request: MembershipPlanRequest) -> MembershipPlanResponse:
    code = _normalize_code(request.code)
    if db.scalar(select(MembershipPlan).where(MembershipPlan.code == code)) is not None:
        raise ValueError(f"Plan code '{code}' already exists")
    if not request.name or not request.name.strip():
        raise ValueError("Plan name is required")
    plan = MembershipPlan(
        code=code, name=request.name.strip(), description=request.description,
        price=max(request.price or 0, 0), duration_days=max(request.durationDays or 30, 0),
        max_session_minutes=max(request.maxSessionMinutes or 0, 0),
        unlimited_access=bool(request.unlimitedAccess), trainer_chat=bool(request.trainerChat),
        features=request.features, active=request.active if request.active is not None else True,
    )
    db.add(plan)
    db.commit()
    return _to_response(plan)


def update(db: Session, id_: int, request: MembershipPlanRequest) -> MembershipPlanResponse:
    plan = db.get(MembershipPlan, id_)
    if plan is None:
        raise InvalidOperationException(f"Membership plan {id_} not found")
    if request.code is not None:
        code = _normalize_code(request.code)
        dup = db.scalar(select(MembershipPlan).where(MembershipPlan.code == code))
        if dup is not None and dup.id != id_:
            raise ValueError(f"Plan code '{code}' already exists")
        plan.code = code
    if request.name is not None:
        if not request.name.strip():
            raise ValueError("Plan name is required")
        plan.name = request.name.strip()
    if request.description is not None:
        plan.description = request.description
    if request.price is not None:
        plan.price = max(request.price, 0)
    if request.durationDays is not None:
        plan.duration_days = max(request.durationDays, 0)
    if request.maxSessionMinutes is not None:
        plan.max_session_minutes = max(request.maxSessionMinutes, 0)
    if request.unlimitedAccess is not None:
        plan.unlimited_access = request.unlimitedAccess
    if request.trainerChat is not None:
        plan.trainer_chat = request.trainerChat
    if request.features is not None:
        plan.features = request.features
    if request.active is not None:
        plan.active = request.active
    db.commit()
    return _to_response(plan)


def delete(db: Session, id_: int) -> None:
    plan = db.get(MembershipPlan, id_)
    if plan is None:
        raise InvalidOperationException(f"Membership plan {id_} not found")
    db.delete(plan)
    db.commit()
