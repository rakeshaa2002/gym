from fastapi import Depends, Header
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.exceptions import ForbiddenException
from app.core.security import extract_email
from app.models.role import Role
from app.models.users import Users

__all__ = ["get_db", "get_current_user", "require_super_admin", "require_role"]


def get_current_user(
    authorization: str | None = Header(default=None),
    db: Session = Depends(get_db),
) -> Users:
    """Mirrors JwtAuthFilter + CustomUserService: validates the bearer token and
    re-fetches the user from the DB on every request (stateless, no sessions)."""
    if not authorization or not authorization.startswith("Bearer "):
        raise ForbiddenException("Authorization token required")

    token = authorization[7:].strip()
    email = extract_email(token)
    if not email:
        raise ForbiddenException("Invalid token user")

    user = db.scalar(select(Users).where(Users.email == email))
    if user is None:
        raise ForbiddenException("Invalid token user")
    return user


def require_super_admin(user: Users = Depends(get_current_user)) -> Users:
    if Role(user.role) != Role.SUPER_ADMIN:
        raise ForbiddenException("Only Super Admin can manage permissions")
    return user


def require_role(*allowed_roles: str):
    """Mirrors OrganizationHierarchyController.requireRole: role compared case-insensitively."""
    allowed = {r.upper() for r in allowed_roles}

    def _dep(user: Users = Depends(get_current_user)) -> Users:
        role = str(user.role).upper()
        if role not in allowed:
            raise ForbiddenException(f"Unauthorized: Role {role} cannot perform this action")
        return user

    return _dep
