from sqlalchemy import Boolean, Integer, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base
from app.models.role import Role


class PermissionPage(Base):
    __tablename__ = "permission_pages"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    page_key: Mapped[str] = mapped_column(String(100), unique=True, nullable=False)
    label: Mapped[str] = mapped_column(String(150), nullable=False)
    route_path: Mapped[str] = mapped_column(String(150), nullable=False)
    category: Mapped[str] = mapped_column(String(80), nullable=False)
    sort_order: Mapped[int] = mapped_column(Integer, nullable=False, default=0)


class RolePagePermission(Base):
    __tablename__ = "role_page_permissions"
    __table_args__ = (
        UniqueConstraint("role_name", "page_key", name="uk_role_page_permission"),
    )

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    role: Mapped[Role] = mapped_column("role_name", String(20), nullable=False)
    page_key: Mapped[str] = mapped_column(String(100), nullable=False)
    can_view: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    can_create: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    can_edit: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    can_delete: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
