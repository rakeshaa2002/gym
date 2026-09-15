from datetime import datetime

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base
from app.models.role import Role


class Users(Base):
    """Mirrors entity/Users.java — table `user_accounts`, the single source of truth for auth."""

    __tablename__ = "user_accounts"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    email: Mapped[str] = mapped_column(String(100), unique=True, nullable=False)
    password: Mapped[str] = mapped_column(String, nullable=False)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    role: Mapped[Role] = mapped_column(String(20), nullable=False)

    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True)
    is_approved: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)

    created_by_id: Mapped[int | None] = mapped_column(ForeignKey("user_accounts.id"))
    reports_to_id: Mapped[int | None] = mapped_column(ForeignKey("user_accounts.id"))
    admin_id: Mapped[int | None] = mapped_column(ForeignKey("user_accounts.id"))
    manager_id: Mapped[int | None] = mapped_column(ForeignKey("user_accounts.id"))
    trainer_id: Mapped[int | None] = mapped_column(ForeignKey("user_accounts.id"))

    # Forward references to domains built in later phases (fitness/diet) — plain
    # columns without FK constraints until those tables exist.
    assigned_diet_plan_id: Mapped[int | None] = mapped_column(Integer)
    assigned_workout_plan_id: Mapped[int | None] = mapped_column(Integer)

    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, default=datetime.now)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, nullable=False, default=datetime.now, onupdate=datetime.now
    )
    last_login_at: Mapped[datetime | None] = mapped_column(DateTime)
    last_seen_at: Mapped[datetime | None] = mapped_column(DateTime)
    onboarding_completed_at: Mapped[datetime | None] = mapped_column(DateTime)

    head_office_id: Mapped[int | None] = mapped_column(Integer)
    branch_id: Mapped[int | None] = mapped_column(Integer)
    department_id: Mapped[int | None] = mapped_column(Integer)
    team_id: Mapped[int | None] = mapped_column(Integer)
    designation_id: Mapped[int | None] = mapped_column(Integer)

    fingerprint_id: Mapped[str | None] = mapped_column(String(100), unique=True)
    qr_token: Mapped[str | None] = mapped_column(String(64), unique=True)
    email_verified: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)


# SUPER_ADMIN=5 > ADMIN=4 > MANAGER=3 > TRAINER=COUNSELOR=2 > USER=1
ROLE_LEVEL: dict[Role, int] = {
    Role.SUPER_ADMIN: 5,
    Role.ADMIN: 4,
    Role.MANAGER: 3,
    Role.TRAINER: 2,
    Role.COUNSELOR: 2,
    Role.USER: 1,
    Role.CORPORATE_HR: 1,
}
