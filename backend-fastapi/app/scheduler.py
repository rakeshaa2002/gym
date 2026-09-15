import logging

from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger

from app.core.config import settings

log = logging.getLogger("fitnexus.scheduler")

scheduler = BackgroundScheduler()


def start_scheduler() -> None:
    from app.services.attendance_overstay_scheduler import check_overstays

    scheduler.add_job(
        check_overstays, "interval", seconds=settings.attendance_overstay_check_ms / 1000,
        id="attendance_overstay_check",
    )

    from app.services.churn_notification_scheduler import run_daily_churn_check

    scheduler.add_job(run_daily_churn_check, CronTrigger(hour=10, minute=0), id="daily_churn_check")

    if not scheduler.running:
        scheduler.start()
        log.info("APScheduler started with %d job(s)", len(scheduler.get_jobs()))
