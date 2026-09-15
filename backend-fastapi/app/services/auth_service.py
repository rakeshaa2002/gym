import logging

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.exceptions import (
    DuplicateResourceException,
    InvalidOperationException,
    UnauthorizedException,
)
from app.core.security import create_access_token, hash_password, verify_password
from app.models.fitness_user import FitnessUser
from app.models.role import Role
from app.models.users import Users
from app.schemas.auth import LoginRequest, LoginResponse, RegisterRequest, RegisterResponse
from datetime import datetime

log = logging.getLogger("fitnexus.auth")


def register(db: Session, request: RegisterRequest) -> RegisterResponse:
    log.info("Processing registration for email: %s", request.email)

    existing = db.scalar(select(Users).where(Users.email == request.email))
    if existing is not None:
        log.warning("Registration failed: Email already exists: %s", request.email)
        raise DuplicateResourceException(f"Email '{request.email}' is already registered")

    account = Users(
        email=request.email,
        name=request.name,
        password=hash_password(request.password),
        role=Role.USER,
        is_active=False,
        is_approved=False,
    )
    db.add(account)
    db.flush()

    fitness_user = FitnessUser(id=account.id, details_completed=False, registration_date=datetime.now())
    db.add(fitness_user)
    db.commit()

    log.info("User registered successfully with ID: %s", fitness_user.id)
    return RegisterResponse(
        id=fitness_user.id,
        email=account.email,
        name=account.name,
        message="Registration successful! Please wait for trainer activation",
        success=True,
    )


def login(db: Session, request: LoginRequest) -> LoginResponse:
    log.info("Processing login request for email: %s", request.email)

    found_user = db.scalar(select(Users).where(Users.email == request.email))
    if found_user is None:
        log.warning("Login failed: User not found with email: %s", request.email)
        raise UnauthorizedException("Invalid email or password")

    if not verify_password(request.password, found_user.password):
        log.warning("Login failed: Invalid password for email: %s", request.email)
        raise UnauthorizedException("Invalid email or password")

    if not found_user.is_active:
        log.warning("Login attempted by inactive user: %s", request.email)
        raise InvalidOperationException("User account is not activated. Please contact your trainer.")

    if found_user.role == Role.USER and not found_user.is_approved:
        log.warning("Login attempted by unapproved user: %s", request.email)
        raise InvalidOperationException("User account is not approved yet. Please wait for trainer approval.")

    token = create_access_token(found_user.email, found_user.role)

    found_user.last_login_at = datetime.now()
    db.commit()

    log.info("User logged in successfully: %s with role: %s", request.email, found_user.role)
    return LoginResponse(
        userId=found_user.id,
        email=found_user.email,
        name=found_user.name,
        token=token,
        role=found_user.role,
    )
