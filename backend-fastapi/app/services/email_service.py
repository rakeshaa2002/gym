import logging
import smtplib
from email.mime.text import MIMEText

from app.core.config import settings

log = logging.getLogger("fitnexus.email")


def is_configured() -> bool:
    return bool(settings.mail_host)


def send(to: str, subject: str, body: str) -> None:
    if not is_configured():
        log.warning("[EMAIL DEMO MODE] To: %s | %s | %s", to, subject, body)
        return
    try:
        msg = MIMEText(body)
        msg["From"] = settings.mail_from
        msg["To"] = to
        msg["Subject"] = subject
        with smtplib.SMTP(settings.mail_host, settings.mail_port) as server:
            server.starttls()
            if settings.mail_username:
                server.login(settings.mail_username, settings.mail_password)
            server.sendmail(settings.mail_from, [to], msg.as_string())
        log.info("Email sent to %s", to)
    except Exception as e:
        log.error("Failed to send email to %s: %s", to, e)
        raise RuntimeError(f"Could not send email: {e}") from e


def send_otp(to: str, otp: str, purpose_label: str) -> None:
    subject = f"FitNexus {purpose_label} code"
    body = (
        f"Your FitNexus {purpose_label} code is: {otp}\n\n"
        "This code expires shortly. If you didn't request it, please ignore this email."
    )
    send(to, subject, body)
