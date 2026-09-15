import logging
from datetime import datetime

from fastapi import APIRouter, Depends, Request
from fastapi.responses import JSONResponse
from pydantic import ValidationError
from sqlalchemy.orm import Session

from app.api.deps import get_db
from app.core.exceptions import DuplicateResourceException, InvalidOperationException, UnauthorizedException
from app.schemas.auth import (
    ForgotPasswordRequest,
    LoginRequest,
    RegisterRequest,
    ResetPasswordRequest,
    VerifyOtpRequest,
)
from app.schemas.common import success
from app.services import auth_otp_service, auth_service

log = logging.getLogger("fitnexus.auth")

router = APIRouter(prefix="/api/auth", tags=["auth"])


def _err(status: int, path: str, message: str) -> JSONResponse:
    return JSONResponse(
        status_code=status,
        content={"status": status, "message": message, "timestamp": datetime.now().isoformat(), "path": path},
    )


async def _parse_or_400(request: Request, model, path: str):
    """Mirrors the Java controllers' @Valid + BindingResult pattern: on validation
    failure, return the *first* field error message as a plain string (not a
    fieldErrors map), since the frontend reads response.data.message directly."""
    body = await request.json()
    try:
        return model(**body), None
    except ValidationError as e:
        first = e.errors()[0]
        msg = first.get("msg", "Invalid request")
        msg = msg.removeprefix("Value error, ")
        return None, _err(400, path, msg)


@router.post("/forgot-password")
def forgot_password(request: ForgotPasswordRequest, db: Session = Depends(get_db)):
    try:
        result = auth_otp_service.forgot_password(db, request.email if request else None)
        return success(result.model_dump(), "If an account exists for that email, a reset code has been sent.")
    except ValueError as e:
        return _err(400, "/api/auth/forgot-password", str(e))
    except Exception as e:
        log.error("forgot-password error", exc_info=True)
        return _err(500, "/api/auth/forgot-password", f"Could not process request: {e}")


@router.post("/verify-otp")
def verify_otp(request: VerifyOtpRequest, db: Session = Depends(get_db)):
    try:
        valid = request is not None and auth_otp_service.verify_otp(db, request.email, request.otp, request.purpose)
        if not valid:
            return _err(400, "/api/auth/verify-otp", "Invalid or expired code")
        return success({"verified": True}, "Code verified")
    except Exception as e:
        log.error("verify-otp error", exc_info=True)
        return _err(500, "/api/auth/verify-otp", f"Could not verify code: {e}")


@router.post("/reset-password")
def reset_password(request: ResetPasswordRequest, db: Session = Depends(get_db)):
    try:
        auth_otp_service.reset_password(db, request.email, request.otp, request.newPassword)
        return success(None, "Password updated successfully. You can now sign in.")
    except ValueError as e:
        return _err(400, "/api/auth/reset-password", str(e))
    except Exception as e:
        log.error("reset-password error", exc_info=True)
        return _err(500, "/api/auth/reset-password", f"Could not reset password: {e}")


@router.post("/send-email-otp")
def send_email_otp(request: ForgotPasswordRequest, db: Session = Depends(get_db)):
    try:
        result = auth_otp_service.send_email_verification(db, request.email if request else None)
        return success(result.model_dump(), "Verification code sent.")
    except ValueError as e:
        return _err(400, "/api/auth/send-email-otp", str(e))
    except Exception as e:
        log.error("send-email-otp error", exc_info=True)
        return _err(500, "/api/auth/send-email-otp", f"Could not send code: {e}")


@router.post("/verify-email")
def verify_email(request: VerifyOtpRequest, db: Session = Depends(get_db)):
    try:
        auth_otp_service.verify_email(db, request.email, request.otp)
        return success(None, "Email verified successfully.")
    except ValueError as e:
        return _err(400, "/api/auth/verify-email", str(e))
    except Exception as e:
        log.error("verify-email error", exc_info=True)
        return _err(500, "/api/auth/verify-email", f"Could not verify email: {e}")


@router.post("/login")
async def login(request: Request, db: Session = Depends(get_db)):
    payload, error = await _parse_or_400(request, LoginRequest, "/api/auth/login")
    if error:
        return error
    try:
        response = auth_service.login(db, payload)
        log.info("User logged in successfully: %s", payload.email)
        return success(response.model_dump(), "Login successful")
    except (UnauthorizedException, InvalidOperationException):
        raise
    except Exception as e:
        log.error("Login error", exc_info=True)
        return _err(500, "/api/auth/login", f"An error occurred during login: {e}")


@router.post("/signup", status_code=201)
async def signup(request: Request, db: Session = Depends(get_db)):
    payload, error = await _parse_or_400(request, RegisterRequest, "/api/auth/signup")
    if error:
        return error
    try:
        response = auth_service.register(db, payload)
        log.info("User registered successfully: %s", payload.email)
        return JSONResponse(
            status_code=201,
            content=success(response.model_dump(), response.message, status=201),
        )
    except DuplicateResourceException:
        raise
    except Exception as e:
        log.error("Registration error", exc_info=True)
        return _err(500, "/api/auth/signup", f"An error occurred during registration: {e}")
