import secrets
from datetime import datetime, timedelta

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.security import hash_password
from app.models.otp import EmailOtp
from app.models.users import Users
from app.schemas.auth import OtpDispatchResponse
from app.services import email_service

RESET = "RESET"
VERIFY = "VERIFY"


def _norm(email: str | None) -> str:
    return (email or "").strip().lower()


def _generate_otp() -> str:
    return f"{secrets.randbelow(1_000_000):06d}"


def _create_otp(db: Session, email: str, purpose: str) -> str:
    otp = EmailOtp(
        email=email,
        otp=_generate_otp(),
        purpose=purpose,
        expires_at=datetime.now() + timedelta(minutes=settings.otp_expiry_minutes),
        used=False,
        created_at=datetime.now(),
    )
    db.add(otp)
    db.commit()
    return otp.otp


def _dispatch(otp: str) -> OtpDispatchResponse:
    delivered = email_service.is_configured()
    return OtpDispatchResponse(delivered=delivered, devOtp=None if delivered else otp)


def forgot_password(db: Session, raw_email: str | None) -> OtpDispatchResponse:
    email = _norm(raw_email)
    if not email:
        raise ValueError("Email is required")

    user = db.scalar(select(Users).where(Users.email == email))
    if user is None:
        # Pretend success; nothing sent (don't reveal whether the email exists).
        return OtpDispatchResponse(delivered=email_service.is_configured(), devOtp=None)

    otp = _create_otp(db, email, RESET)
    email_service.send_otp(email, otp, "password reset")
    return _dispatch(otp)


def send_email_verification(db: Session, raw_email: str | None) -> OtpDispatchResponse:
    email = _norm(raw_email)
    user = db.scalar(select(Users).where(Users.email == email))
    if user is None:
        raise ValueError("No account found for this email")

    otp = _create_otp(db, email, VERIFY)
    email_service.send_otp(email, otp, "email verification")
    return _dispatch(otp)


def _find_latest_unused(db: Session, email: str, purpose: str) -> EmailOtp | None:
    return db.scalar(
        select(EmailOtp)
        .where(EmailOtp.email == _norm(email), EmailOtp.purpose == purpose, EmailOtp.used.is_(False))
        .order_by(EmailOtp.id.desc())
        .limit(1)
    )


def _is_valid(db: Session, email: str, otp: str | None, purpose: str) -> bool:
    if not otp or not otp.strip():
        return False
    record = _find_latest_unused(db, email, purpose)
    if record is None:
        return False
    if record.expires_at <= datetime.now():
        return False
    return record.otp == otp.strip()


def verify_otp(db: Session, email: str | None, otp: str | None, purpose: str | None) -> bool:
    p = VERIFY if (purpose or "").upper() == VERIFY else RESET
    return _is_valid(db, email or "", otp, p)


def reset_password(db: Session, email: str | None, otp: str | None, new_password: str | None) -> None:
    if not new_password or len(new_password) < 6:
        raise ValueError("Password must be at least 6 characters")

    record = _find_latest_unused(db, email or "", RESET)
    if (
        record is None
        or record.expires_at <= datetime.now()
        or record.otp != (otp.strip() if otp else None)
    ):
        raise ValueError("Invalid or expired code")

    user = db.scalar(select(Users).where(Users.email == _norm(email)))
    if user is None:
        raise ValueError("Account not found")

    user.password = hash_password(new_password)
    record.used = True
    db.commit()


def verify_email(db: Session, email: str | None, otp: str | None) -> None:
    record = _find_latest_unused(db, email or "", VERIFY)
    if (
        record is None
        or record.expires_at <= datetime.now()
        or record.otp != (otp.strip() if otp else None)
    ):
        raise ValueError("Invalid or expired code")

    user = db.scalar(select(Users).where(Users.email == _norm(email)))
    if user is None:
        raise ValueError("Account not found")

    user.email_verified = True
    record.used = True
    db.commit()
