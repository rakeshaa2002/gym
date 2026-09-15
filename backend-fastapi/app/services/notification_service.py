from datetime import datetime, timedelta

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models.fitness_user import FitnessUser
from app.models.notification import Notification
from app.models.role import Role
from app.models.users import Users
from app.schemas.notification import NotificationResponse

_ONBOARDING_WINDOW_DAYS = 7
_STAFF_ROLES = {Role.SUPER_ADMIN, Role.ADMIN, Role.MANAGER, Role.TRAINER, Role.CORPORATE_HR, Role.COUNSELOR}


def create_for_user(db: Session, recipient_id: int, type_: str, title: str, message: str | None = None, link: str | None = None) -> None:
    if recipient_id is None:
        return
    db.add(Notification(recipient_id=recipient_id, type=type_, title=title, message=message, link=link))
    db.commit()


def _fmt(dt: datetime | None) -> str:
    # Cross-platform equivalent of Java's "MMM d, HH:mm" (no zero-padded day) —
    # %-d is Unix-only in strftime and raises ValueError on Windows.
    if not dt:
        return ""
    return f"{dt.strftime('%b')} {dt.day}, {dt.strftime('%H:%M')}"


def _persisted_for(db: Session, user_id: int) -> list[NotificationResponse]:
    rows = db.scalars(
        select(Notification).where(Notification.recipient_id == user_id).order_by(Notification.created_at.desc()).limit(50)
    )
    return [
        NotificationResponse(key=f"n-{n.id}", type=n.type, title=n.title, message=n.message, time=_fmt(n.created_at), link=n.link)
        for n in rows
    ]


def _display_name(fu: FitnessUser) -> str:
    name = " ".join(p for p in (fu.first_name, fu.last_name) if p and p.strip())
    if name:
        return name
    if fu.account and fu.account.name:
        return fu.account.name
    return fu.account.email if fu.account else "Member"


def _visible_customers_for(db: Session, me: Users) -> list[FitnessUser]:
    role = Role(me.role)
    if role == Role.SUPER_ADMIN:
        return list(db.scalars(select(FitnessUser)))
    if role == Role.ADMIN:
        return list(db.scalars(select(FitnessUser).join(Users, FitnessUser.id == Users.id).where(Users.admin_id == me.id)))
    if role == Role.MANAGER:
        return list(db.scalars(select(FitnessUser).join(Users, FitnessUser.id == Users.id).where(Users.manager_id == me.id)))
    if role == Role.CORPORATE_HR:
        return list(db.scalars(select(FitnessUser).where(FitnessUser.assigned_corporate_hr_id == me.id)))
    if role == Role.TRAINER:
        by_assigned = set(db.scalars(select(FitnessUser.id).where(FitnessUser.assigned_trainer_id == me.id)))
        by_trainer = set(db.scalars(select(FitnessUser.id).join(Users, FitnessUser.id == Users.id).where(Users.trainer_id == me.id)))
        by_created = set(db.scalars(select(FitnessUser.id).join(Users, FitnessUser.id == Users.id).where(Users.created_by_id == me.id)))
        ids = by_assigned | by_trainer | by_created
        return list(db.scalars(select(FitnessUser).where(FitnessUser.id.in_(ids)))) if ids else []
    return []


def _staff_notifications(db: Session, me: Users) -> list[NotificationResponse]:
    visible = _visible_customers_for(db, me)
    result = _persisted_for(db, me.id)

    for c in visible:
        if c.account is not None and c.account.is_approved is False:
            email = c.account.email
            result.append(
                NotificationResponse(
                    key=f"approval-{c.id}", type="APPROVAL", title="Pending approval",
                    message=_display_name(c) + (f" · {email}" if email else ""), time="Awaiting approval", link="/users",
                )
            )

    since = datetime.now() - timedelta(days=_ONBOARDING_WINDOW_DAYS)
    onboarded = sorted(
        (c for c in visible if c.account and c.account.onboarding_completed_at and c.account.onboarding_completed_at > since),
        key=lambda c: c.account.onboarding_completed_at, reverse=True,
    )
    for c in onboarded:
        result.append(
            NotificationResponse(
                key=f"onboard-{c.id}", type="INFO", title="Onboarding completed",
                message=f"{_display_name(c)} finished setting up their profile",
                time=_fmt(c.account.onboarding_completed_at), link="/users",
            )
        )
    return result


def _member_notifications(db: Session, me: Users) -> list[NotificationResponse]:
    # Upcoming-session derivation requires the full UserWorkoutSchedule model
    # (start_date_time/status/title/location) built out in the Fitness domain phase.
    return _persisted_for(db, me.id)


def get_my_notifications(db: Session, me: Users) -> list[NotificationResponse]:
    if Role(me.role) in _STAFF_ROLES:
        return _staff_notifications(db, me)
    return _member_notifications(db, me)
