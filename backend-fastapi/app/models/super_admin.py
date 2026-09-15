from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class SuperAdmin(Base):
    __tablename__ = "super_admins"

    id: Mapped[int] = mapped_column("user_id", ForeignKey("user_accounts.id"), primary_key=True)
    account: Mapped["Users"] = relationship("Users")

    first_name: Mapped[str] = mapped_column(String(50), nullable=False)
    last_name: Mapped[str | None] = mapped_column(String(50))
    join_date: Mapped[datetime | None] = mapped_column(DateTime, default=datetime.now)
