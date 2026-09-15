import logging
from datetime import datetime

from sqlalchemy import select

from app.core.database import SessionLocal
from app.models.attendance import Attendance
from app.models.fitness_user import FitnessUser
from app.models.membership import MembershipPlan
from app.models.role import Role
from app.models.users import Users
from app.services import notification_service

log = logging.getLogger("fitnexus.attendance_overstay")


def check_overstays() -> None:
    with SessionLocal() as db:
        open_sessions = list(db.scalars(select(Attendance).where(Attendance.check_out_time.is_(None))))
        for record in open_sessions:
            try:
                _check_one(db, record)
            except Exception:
                log.warning("Overstay check failed for attendance %s", record.id, exc_info=True)


def _check_one(db, record: Attendance) -> None:
    if record.overstay_notified or record.check_in_time is None:
        return
    member = db.get(Users, record.user_id)
    if member is None or Role(member.role) != Role.USER:
        return
    fu = db.get(FitnessUser, member.id)
    plan = db.get(MembershipPlan, fu.membership_plan_id) if fu and fu.membership_plan_id else None
    limit = plan.max_session_minutes if plan else None
    if not limit or limit <= 0:
        return

    minutes = (datetime.now() - record.check_in_time).total_seconds() / 60
    if minutes < limit:
        return

    plan_name = plan.name if plan else "current"
    recipients = {uid for uid in (member.admin_id,) if uid}
    if not recipients:
        recipients = set(
            db.scalars(select(Users.id).where(Users.role.in_([Role.ADMIN.value, Role.SUPER_ADMIN.value])))
        )
    for rid in recipients:
        notification_service.create_for_user(
            db, rid, "INFO", "Member overstay",
            f"{member.name} has been in the gym {int(minutes)} min — over the {limit}-min limit of the "
            f"{plan_name} plan — and hasn't checked out.",
            "/attendance",
        )
    notification_service.create_for_user(
        db, member.id, "INFO", "Session time exceeded",
        f"You've passed your {plan_name} plan's {limit}-minute visit limit. Upgrade your plan to extend "
        "your time at the gym.",
        "/membership",
    )

    record.overstay_notified = True
    record.updated_at = datetime.now()
    db.commit()
