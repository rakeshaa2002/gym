from datetime import date, datetime, time

from sqlalchemy import Boolean, Date, DateTime, Float, ForeignKey, Integer, String, Time
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class FitnessUser(Base):
    """Mirrors entity/FitnessUser.java — table `users` (member profile), 1:1 with user_accounts."""

    __tablename__ = "users"

    id: Mapped[int] = mapped_column(
        "user_id", ForeignKey("user_accounts.id"), primary_key=True
    )
    account: Mapped["Users"] = relationship("Users", foreign_keys=[id])

    first_name: Mapped[str | None] = mapped_column(String(50))
    last_name: Mapped[str | None] = mapped_column(String(50))
    weight: Mapped[float | None] = mapped_column(Float)
    height: Mapped[float | None] = mapped_column(Float)
    blood_group: Mapped[str | None] = mapped_column(String(3))
    age: Mapped[int | None] = mapped_column(Integer)
    gender: Mapped[str | None] = mapped_column(String(20))
    phone: Mapped[str | None] = mapped_column(String(15))
    address: Mapped[str | None] = mapped_column(String(200))
    city: Mapped[str | None] = mapped_column(String(50))
    medical_conditions: Mapped[str | None] = mapped_column(String(500))
    emergency_contact: Mapped[str | None] = mapped_column(String(100))
    emergency_phone: Mapped[str | None] = mapped_column(String(15))
    photo_path: Mapped[str | None] = mapped_column(String(500))
    id_proof_path: Mapped[str | None] = mapped_column(String(500))

    # FK to trainers table, added once the Trainer model exists (Org/People phase).
    assigned_trainer_id: Mapped[int | None] = mapped_column(Integer)

    details_completed: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    registration_date: Mapped[datetime | None] = mapped_column(DateTime)
    activation_date: Mapped[datetime | None] = mapped_column(DateTime)
    bio: Mapped[str | None] = mapped_column(String(500))
    date_of_birth: Mapped[datetime | None] = mapped_column(DateTime)
    fitness_goals: Mapped[str | None] = mapped_column(String(300))
    dietary_preferences: Mapped[str | None] = mapped_column(String(300))
    injuries_or_limitations: Mapped[str | None] = mapped_column(String(500))
    body_fat: Mapped[float | None] = mapped_column(Float)
    is_frozen: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    referred_by: Mapped[str | None] = mapped_column(String(100))
    membership_plan: Mapped[str | None] = mapped_column(String(20), default="BASIC")
    membership_expiry: Mapped[date | None] = mapped_column(Date)

    # FK to membership_plans table, added once that model exists (Membership phase).
    membership_plan_id: Mapped[int | None] = mapped_column(Integer)

    access_start_time: Mapped[time | None] = mapped_column(Time)
    access_end_time: Mapped[time | None] = mapped_column(Time)
    last_slot_request_at: Mapped[datetime | None] = mapped_column(DateTime)

    assigned_corporate_hr_id: Mapped[int | None] = mapped_column(ForeignKey("user_accounts.id"))
    corporate_department: Mapped[str | None] = mapped_column(String(100))
