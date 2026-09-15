import re

from pydantic import BaseModel, field_validator

from app.models.role import Role

# Mirrors Hibernate Validator's @Email — a loose syntax check, not a deliverability/
# reserved-TLD check like pydantic's EmailStr (which would reject admin@fitnexus.test).
EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")
NAME_RE = re.compile(r"^[a-zA-Z\s'-]+$")
PASSWORD_RE = re.compile(r"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$")


def _validate_email(v: str) -> str:
    if not v or not EMAIL_RE.match(v):
        raise ValueError("Email should be valid")
    return v


class LoginRequest(BaseModel):
    email: str
    password: str

    @field_validator("email")
    @classmethod
    def email_valid(cls, v: str) -> str:
        return _validate_email(v)

    @field_validator("password")
    @classmethod
    def password_length(cls, v: str) -> str:
        if not (6 <= len(v) <= 100):
            raise ValueError("Password must be between 6 and 100 characters")
        return v


class LoginResponse(BaseModel):
    userId: int
    email: str
    name: str
    token: str
    role: Role


class RegisterRequest(BaseModel):
    email: str
    password: str
    name: str

    @field_validator("email")
    @classmethod
    def email_valid(cls, v: str) -> str:
        return _validate_email(v)

    @field_validator("password")
    @classmethod
    def password_strength(cls, v: str) -> str:
        if not (6 <= len(v) <= 100):
            raise ValueError("Password must be between 6 and 100 characters")
        if not PASSWORD_RE.match(v):
            raise ValueError(
                "Password must contain at least one uppercase letter, one lowercase letter, "
                "one digit, and one special character"
            )
        return v

    @field_validator("name")
    @classmethod
    def name_valid(cls, v: str) -> str:
        if not (2 <= len(v) <= 100):
            raise ValueError("Name must be between 2 and 100 characters")
        if not NAME_RE.match(v):
            raise ValueError("Name should only contain letters, spaces, hyphens, and apostrophes")
        return v


class RegisterResponse(BaseModel):
    id: int | None
    email: str
    name: str
    message: str
    success: bool


class ForgotPasswordRequest(BaseModel):
    email: str | None = None


class VerifyOtpRequest(BaseModel):
    email: str | None = None
    otp: str | None = None
    purpose: str | None = None


class ResetPasswordRequest(BaseModel):
    email: str | None = None
    otp: str | None = None
    newPassword: str | None = None


class OtpDispatchResponse(BaseModel):
    delivered: bool
    devOtp: str | None = None
