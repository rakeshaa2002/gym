import logging

from sqlalchemy import select

from app.core.config import settings
from app.core.database import SessionLocal
from app.core.security import hash_password
from app.models.permission import PermissionPage, RolePagePermission
from app.models.role import Role
from app.models.super_admin import SuperAdmin
from app.models.users import Users

log = logging.getLogger("fitnexus.bootstrap")


def _normalize(value: str | None) -> str | None:
    if value is None:
        return None
    value = value.strip()
    return value or None


def _display_name(first: str | None, last: str | None) -> str:
    first, last = _normalize(first), _normalize(last)
    if first is None:
        return last or ""
    return f"{first} {last}" if last else first


def bootstrap_super_admins() -> None:
    with SessionLocal() as db:
        for email in (settings.super_admin_email, settings.super_admin_secondary_email):
            _create_super_admin_if_missing(db, email)


def _create_super_admin_if_missing(db, email: str | None) -> None:
    normalized = _normalize(email)
    if normalized is None:
        return

    if db.scalar(select(Users).where(Users.email == normalized)) is not None:
        log.info("Super admin account already exists for %s", normalized)
        return

    account = Users(
        email=normalized,
        password=hash_password(settings.super_admin_password),
        name=_display_name(settings.super_admin_first_name, settings.super_admin_last_name),
        role=Role.SUPER_ADMIN,
        is_active=True,
        is_approved=True,
    )
    db.add(account)
    db.flush()

    super_admin = SuperAdmin(
        id=account.id,
        first_name=settings.super_admin_first_name,
        last_name=_normalize(settings.super_admin_last_name),
    )
    db.add(super_admin)
    db.commit()
    log.info("Created default super admin account for %s", normalized)


# (page_key, label, route_path, category, sort_order)
_PAGE_SEEDS = [
    ("dashboard", "Overview", "/", "Core", 1),
    ("employees", "Employees", "/employees", "Management", 10),
    ("users", "Users", "/users", "Management", 11),
    ("headoffice", "Head Office", "/headoffice", "Management", 12),
    ("branches", "Branches", "/branches", "Management", 13),
    ("departments", "Departments", "/departments", "Management", 14),
    ("designations", "Designations", "/designations", "Management", 15),
    ("teams", "Teams", "/teams", "Management", 16),
    ("workout-filter", "Workout Filter", "/workout-filter", "Workout", 20),
    ("workout-topfilter", "Workout Top Filter", "/workout-topfilter", "Workout", 21),
    ("upperbody-workout", "Body Workout", "/upperbody-workout", "Workout", 22),
    ("create-workout", "Create Workout", "/create-workout", "Workout", 23),
    ("workout-summary", "Workout Summary", "/workout-summary", "Workout", 24),
    ("workout-type", "Workout Type Master", "/workout-type", "Workout", 25),
    ("body-part", "Body Part Master", "/body-part", "Workout", 26),
    ("exercise-master", "Exercise Master", "/exercise-master", "Workout", 27),
    ("workout-plan", "Workout Plan Master", "/workout-plan", "Workout", 28),
    ("workout-detail", "Workout Detail", "/workout-detail", "Workout", 29),
    ("trainer-duty-schedule", "Trainer Duty Schedule", "/trainer-duty-schedule", "Schedule", 40),
    ("user-workout-schedule", "User Workout Schedule", "/user-workout-schedule", "Schedule", 41),
    ("my-schedule", "My Schedule", "/my-schedule", "Schedule", 42),
    ("dietplan", "Diet Menu", "/dietplan", "Diet Menu", 30),
    ("diet-detail", "Diet Detail", "/diet-detail", "Diet Menu", 31),
    ("goals", "Goals", "/goals", "Fitness", 40),
    ("schedule", "My Schedule", "/schedule", "Fitness", 41),
    ("progress", "Progress", "/progress", "Fitness", 42),
    ("wellness-chat", "Wellness Chat", "/wellness-chat", "Fitness", 43),
    ("profile", "Profile", "/profile", "Account", 50),
    ("onboding-step", "Step", "/onboding-step", "Account", 51),
    ("role-permissions", "Role Permissions", "/role-permissions", "Management", 5),
    ("attendance", "Attendance", "/attendance", "Management", 6),
    ("membership-plans", "Membership Plans", "/membership-plans", "Management", 7),
    ("leads", "Leads CRM", "/leads", "Management", 8),
    ("billing", "Billing", "/billing", "Management", 32),
    ("inventory", "Inventory", "/inventory", "Management", 33),
    ("reports", "Reports", "/reports", "Management", 34),
]

# role -> [(page_key, view, create, edit, delete), ...]
_DEFAULT_PERMISSIONS: dict[Role, list[tuple[str, bool, bool, bool, bool]]] = {
    Role.SUPER_ADMIN: [(key, True, True, True, True) for key, *_ in _PAGE_SEEDS if key != "my-schedule"]
    + [("my-schedule", True, False, False, False)],
    Role.ADMIN: [
        ("dashboard", True, False, False, False),
        ("employees", True, True, True, True),
        ("users", True, True, True, True),
        ("headoffice", True, True, True, True),
        ("branches", True, True, True, True),
        ("departments", True, True, True, True),
        ("designations", True, True, True, True),
        ("teams", True, True, True, True),
        ("schedule", True, False, False, False),
        ("profile", True, False, False, False),
        ("onboding-step", True, False, False, False),
        ("workout-type", True, True, True, True),
        ("body-part", True, True, True, True),
        ("exercise-master", True, True, True, True),
        ("workout-plan", True, True, True, True),
        ("workout-detail", True, True, True, True),
        ("attendance", True, True, True, True),
        ("membership-plans", True, True, True, True),
        ("wellness-chat", True, False, False, False),
        ("leads", True, True, True, True),
        ("billing", True, True, True, True),
        ("inventory", True, True, True, True),
        ("reports", True, True, True, True),
    ],
    Role.MANAGER: [
        ("dashboard", True, False, False, False),
        ("employees", True, True, True, True),
        ("users", True, True, True, True),
        ("branches", True, True, True, True),
        ("departments", True, True, True, True),
        ("designations", True, True, True, True),
        ("teams", True, True, True, True),
        ("schedule", True, False, False, False),
        ("profile", True, False, False, False),
        ("onboding-step", True, False, False, False),
        ("workout-type", True, True, True, True),
        ("body-part", True, True, True, True),
        ("exercise-master", True, True, True, True),
        ("workout-plan", True, True, True, True),
        ("workout-detail", True, True, True, True),
        ("trainer-duty-schedule", True, True, True, True),
        ("user-workout-schedule", True, True, True, True),
        ("my-schedule", False, False, False, False),
        ("attendance", True, True, True, True),
        ("membership-plans", True, True, True, True),
        ("wellness-chat", True, False, False, False),
        ("leads", True, True, True, True),
        ("billing", True, True, True, True),
        ("inventory", True, True, True, True),
        ("reports", True, True, True, True),
    ],
    Role.TRAINER: [
        ("dashboard", True, False, False, False),
        ("users", True, True, True, True),
        ("workout-filter", True, False, False, False),
        ("workout-topfilter", True, False, False, False),
        ("upperbody-workout", True, False, False, False),
        ("create-workout", True, True, True, True),
        ("workout-summary", True, False, False, False),
        ("workout-type", True, True, True, True),
        ("body-part", True, True, True, True),
        ("exercise-master", True, True, True, True),
        ("workout-plan", True, True, True, True),
        ("workout-detail", True, True, True, True),
        ("trainer-duty-schedule", False, False, False, False),
        ("user-workout-schedule", True, True, True, True),
        ("my-schedule", False, False, False, False),
        ("dietplan", True, False, False, False),
        ("diet-detail", True, False, False, False),
        ("goals", True, False, False, False),
        ("schedule", True, False, False, False),
        ("progress", True, False, False, False),
        ("profile", True, False, False, False),
        ("onboding-step", True, False, False, False),
        ("wellness-chat", True, True, False, False),
        ("attendance", True, True, True, True),
        ("leads", True, False, False, False),
        ("reports", True, False, False, False),
    ],
    Role.USER: [
        ("dashboard", True, False, False, False),
        ("workout-detail", True, False, False, False),
        ("trainer-duty-schedule", False, False, False, False),
        ("user-workout-schedule", False, False, False, False),
        ("my-schedule", True, False, False, False),
        ("dietplan", True, False, False, False),
        ("diet-detail", True, False, False, False),
        ("goals", True, False, False, False),
        ("schedule", True, False, False, False),
        ("progress", True, False, False, False),
        ("profile", True, False, False, False),
        ("onboding-step", True, False, False, False),
        ("wellness-chat", True, True, False, False),
        ("attendance", True, False, False, False),
    ],
    Role.COUNSELOR: [
        ("leads", True, True, True, True),
    ],
}


def bootstrap_permissions() -> None:
    with SessionLocal() as db:
        _seed_pages(db)
        _seed_default_permissions(db)
        db.commit()


def _seed_pages(db) -> None:
    for page_key, label, route_path, category, sort_order in _PAGE_SEEDS:
        page = db.scalar(select(PermissionPage).where(PermissionPage.page_key == page_key))
        if page is None:
            page = PermissionPage(page_key=page_key)
            db.add(page)
        page.label = label
        page.route_path = route_path
        page.category = category
        page.sort_order = sort_order


def _seed_default_permissions(db) -> None:
    for role, seeds in _DEFAULT_PERMISSIONS.items():
        for page_key, view, create, edit, delete in seeds:
            existing = db.scalar(
                select(RolePagePermission).where(
                    RolePagePermission.role == role, RolePagePermission.page_key == page_key
                )
            )
            if existing is not None:
                continue
            db.add(
                RolePagePermission(
                    role=role, page_key=page_key, can_view=view, can_create=create, can_edit=edit, can_delete=delete
                )
            )
