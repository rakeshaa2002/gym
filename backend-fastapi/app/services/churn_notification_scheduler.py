import logging
from datetime import date, datetime, timedelta

from sqlalchemy import select

from app.core.database import SessionLocal
from app.models.attendance import Attendance
from app.models.fitness_user import FitnessUser
from app.models.notification import Notification
from app.services import notification_service

log = logging.getLogger("fitnexus.churn_scheduler")

_EXPIRY_MILESTONES = {30, 15, 7, 3, 0}


def run_daily_churn_check() -> None:
    log.info("Running daily churn and renewal notification check...")
    with SessionLocal() as db:
        today = date.today()
        for fu in db.scalars(select(FitnessUser)):
            if fu.account is None or not fu.account.is_active:
                continue

            if fu.membership_expiry is not None:
                days_left = (fu.membership_expiry - today).days
                if days_left in _EXPIRY_MILESTONES:
                    _send_expiry_notification(db, fu, days_left)

            last_visit = db.scalar(
                select(Attendance.check_in_time)
                .where(Attendance.user_id == fu.id)
                .order_by(Attendance.check_in_time.desc())
                .limit(1)
            )
            if last_visit is not None and (today - last_visit.date()).days == 10:
                _send_irregular_attendance_notification(db, fu)

            # Workout-completion-rate check requires UserWorkoutSchedule.completion_status
            # / start_date_time, added when the Fitness domain phase fleshes out that model.
        log.info("Completed daily churn and renewal notification check.")


def _already_sent_since(db, recipient_id: int, type_: str, since: datetime) -> bool:
    return (
        db.scalar(
            select(Notification.id).where(
                Notification.recipient_id == recipient_id, Notification.type == type_, Notification.created_at > since
            ).limit(1)
        )
        is not None
    )


def _send_irregular_attendance_notification(db, fu: FitnessUser) -> None:
    start_of_day = datetime.combine(date.today(), datetime.min.time())
    if _already_sent_since(db, fu.id, "WARNING", start_of_day - timedelta(days=7)):
        return
    notification_service.create_for_user(
        db, fu.id, "WARNING", "We Miss You!",
        "You haven't visited the gym in 10 days. Consistency is key to reaching your goals! Come in today!",
        "/schedule",
    )
    log.info("Auto WhatsApp to %s: Hi %s, we miss you! You haven't visited in 10 days.",
              fu.phone or fu.account.email, fu.first_name)


def _send_expiry_notification(db, fu: FitnessUser, days_left: int) -> None:
    start_of_day = datetime.combine(date.today(), datetime.min.time())
    if _already_sent_since(db, fu.id, "INFO", start_of_day):
        return
    message = (
        "Your membership expires TODAY. Renew now and get a 10% discount!"
        if days_left == 0
        else f"Your membership expires in {days_left} days. Renew now and get a 10% discount!"
    )
    notification_service.create_for_user(db, fu.id, "INFO", "Membership Expiring Soon", message, "/membership")
    log.info("Auto WhatsApp to %s: Hi %s, your membership expires on %s.",
              fu.phone or fu.account.email, fu.first_name, fu.membership_expiry)
