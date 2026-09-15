import logging
from datetime import date, datetime, timedelta

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.exceptions import EntityNotFoundException, ForbiddenException, InvalidOperationException
from app.core.security import hash_password
from app.models.fitness_user import FitnessUser
from app.models.role import Role
from app.models.staff import HR_PROFILE_FIELDS, Admin, Counselor, Manager, Trainer
from app.models.super_admin import SuperAdmin
from app.models.users import ROLE_LEVEL, Users
from app.schemas.staff import (
    AdminResponse,
    AtRiskMemberResponse,
    CorporateHrResponse,
    CounselorResponse,
    CreateAdminRequest,
    CreateCorporateHrRequest,
    CreateCounselorRequest,
    CreateCustomerByTrainerRequest,
    CreateCustomerSelfRequest,
    CreateSuperAdminRequest,
    CreateTrainerRequest,
    CustomerResponse,
    MyProfileRequest,
    MyProfileResponse,
    ReportingOptionResponse,
    SuperAdminResponse,
    TrainerResponse,
    UpdateCustomerDetailsRequest,
)

log = logging.getLogger("fitnexus.user_management")


# ---------- role hierarchy ----------


def _role_level(role) -> int:
    return ROLE_LEVEL.get(Role(role), 0)


def _get_user_or_404(db: Session, user_id: int) -> Users:
    user = db.get(Users, user_id)
    if user is None:
        raise EntityNotFoundException(f"User {user_id} not found")
    return user


def check_create_permission(db: Session, creator_id: int, role_to_create: Role) -> Users:
    creator = _get_user_or_404(db, creator_id)
    if _role_level(role_to_create) >= _role_level(creator.role):
        raise ForbiddenException(f"Cannot create a {role_to_create.value} account")
    return creator


def check_update_permission(db: Session, updater_id: int, target_user_id: int) -> None:
    if updater_id == target_user_id:
        return
    updater = _get_user_or_404(db, updater_id)
    target = _get_user_or_404(db, target_user_id)
    if _role_level(target.role) >= _role_level(updater.role):
        raise ForbiddenException("Not permitted to update this account")


def check_delete_permission(db: Session, deleter_id: int, target_user_id: int) -> None:
    if deleter_id == target_user_id:
        raise ForbiddenException("Cannot delete your own account")
    check_update_permission(db, deleter_id, target_user_id)


def check_view_permission(db: Session, requester_id: int, target_user_id: int) -> None:
    if requester_id == target_user_id:
        return
    requester = _get_user_or_404(db, requester_id)
    target = _get_user_or_404(db, target_user_id)
    if _role_level(target.role) >= _role_level(requester.role):
        raise ForbiddenException("Not permitted to view this account")


def ensure_requester_can_list(db: Session, requester_id: int, listed_role: Role) -> Users:
    requester = _get_user_or_404(db, requester_id)
    if _role_level(listed_role) >= _role_level(requester.role):
        raise ForbiddenException(f"Not permitted to list {listed_role.value} accounts")
    return requester


_ALLOWED_REPORTS_TO = {
    Role.TRAINER: {Role.MANAGER, Role.ADMIN},
    Role.MANAGER: {Role.ADMIN},
    Role.ADMIN: {Role.SUPER_ADMIN},
}


def apply_reports_to(db: Session, account: Users, reports_to_id: int | None) -> None:
    if reports_to_id is None:
        return
    target = db.get(Users, reports_to_id)
    if target is None:
        raise ValueError("reportsToId does not reference an existing user")
    allowed = _ALLOWED_REPORTS_TO.get(Role(account.role), set())
    if Role(target.role) not in allowed:
        raise ValueError(f"{account.role} cannot report to a {target.role}")
    if Role(target.role) != Role.SUPER_ADMIN and account.branch_id is not None and target.branch_id is not None:
        if account.branch_id != target.branch_id:
            raise ValueError("reportsTo must be in the same branch")
    account.reports_to_id = reports_to_id


def apply_hierarchy_links(account: Users, creator: Users | None) -> None:
    role = Role(account.role)
    if creator is None:
        return
    if role == Role.ADMIN:
        account.admin_id = account.id
    elif role == Role.MANAGER:
        account.admin_id = creator.admin_id or (creator.id if Role(creator.role) == Role.ADMIN else None)
        account.manager_id = account.id
    elif role in (Role.TRAINER, Role.COUNSELOR):
        account.admin_id = creator.admin_id or (creator.id if Role(creator.role) == Role.ADMIN else None)
        account.manager_id = creator.manager_id or (creator.id if Role(creator.role) == Role.MANAGER else None)
        account.trainer_id = account.id
    elif role == Role.USER:
        account.admin_id = creator.admin_id or (creator.id if Role(creator.role) == Role.ADMIN else None)
        account.manager_id = creator.manager_id or (creator.id if Role(creator.role) == Role.MANAGER else None)
        account.trainer_id = creator.trainer_id or (creator.id if Role(creator.role) == Role.TRAINER else None)


def _build_display_name(first: str | None, last: str | None) -> str:
    first = (first or "").strip() or None
    last = (last or "").strip() or None
    if first is None:
        return last or ""
    return f"{first} {last}" if last else first


def create_account(
    db: Session, email: str, password: str, first_name: str | None, last_name: str | None,
    role: Role, creator: Users | None,
) -> Users:
    if db.scalar(select(Users).where(Users.email == email)) is not None:
        raise ValueError("Email already exists")
    account = Users(
        email=email, password=hash_password(password), name=_build_display_name(first_name, last_name),
        role=role, is_active=True, is_approved=True, created_by_id=creator.id if creator else None,
    )
    db.add(account)
    db.flush()
    apply_hierarchy_links(account, creator)
    return account


def _parse_date(value: str | None) -> date | None:
    if not value or not value.strip():
        return None
    try:
        return date.fromisoformat(value.strip())
    except ValueError:
        return None


def _normalize(value: str | None) -> str | None:
    if value is None:
        return None
    v = value.strip()
    return v or None


_DATE_FIELDS = {"date_of_birth", "probation_end_date", "declaration_date"}


def _camel(snake: str) -> str:
    parts = snake.split("_")
    return parts[0] + "".join(p.title() for p in parts[1:])


def _apply_hr_fields(entity, request) -> None:
    for field in HR_PROFILE_FIELDS:
        value = getattr(request, _camel(field), None)
        setattr(entity, field, _parse_date(value) if field in _DATE_FIELDS else _normalize(value))


def _apply_org_fields(account: Users, request) -> None:
    account.head_office_id = request.headOfficeId
    account.branch_id = request.branchId
    account.department_id = request.departmentId
    account.team_id = request.teamId
    account.designation_id = request.designationId


def _fmt_date(d: date | None) -> str | None:
    return d.isoformat() if d else None


def _resolve_reports_to(db: Session, account: Users) -> tuple[str | None, str | None]:
    if not account.reports_to_id:
        return None, None
    target = db.get(Users, account.reports_to_id)
    if target is None:
        return None, None
    return target.name, str(target.role)


# ---------- Admin ----------


def _to_admin_response(db: Session, entity: Admin) -> AdminResponse:
    account = entity.account
    creator = db.get(Users, account.created_by_id) if account.created_by_id else None
    reports_to_name, reports_to_role = _resolve_reports_to(db, account)
    data = {f: (_fmt_date(getattr(entity, f)) if f in _DATE_FIELDS else getattr(entity, f)) for f in HR_PROFILE_FIELDS}
    return AdminResponse(
        id=entity.id, email=account.email, firstName=entity.first_name, lastName=entity.last_name,
        department=entity.department, phone=entity.phone, employeeId=entity.employee_id,
        qualification=entity.qualification, isActive=bool(account.is_active),
        createdByName=creator.name if creator else None, headOfficeId=account.head_office_id,
        branchId=account.branch_id, departmentId=account.department_id, teamId=account.team_id,
        designationId=account.designation_id, joinDate=entity.join_date, bio=entity.bio,
        reportsToId=account.reports_to_id, reportsToName=reports_to_name, reportsToRole=reports_to_role,
        createdAt=entity.created_at, updatedAt=entity.updated_at,
        **{_camel(k): v for k, v in data.items()},
    )


def create_admin(db: Session, request: CreateAdminRequest, creator_id: int) -> AdminResponse:
    creator = check_create_permission(db, creator_id, Role.ADMIN)
    account = create_account(db, request.email, request.password, request.firstName, request.lastName, Role.ADMIN, creator)
    _apply_org_fields(account, request)
    apply_reports_to(db, account, request.reportsToId)
    entity = Admin(
        id=account.id, first_name=request.firstName, last_name=request.lastName,
        department=request.department, phone=request.phone, employee_id=request.employeeId,
        qualification=request.qualification, join_date=_parse_date(request.joinDate) or datetime.now(),
        bio=request.bio,
    )
    _apply_hr_fields(entity, request)
    entity.account = account
    db.add(entity)
    db.commit()
    return _to_admin_response(db, entity)


def get_admin(db: Session, admin_id: int, requester_id: int) -> AdminResponse:
    check_view_permission(db, requester_id, admin_id)
    entity = db.get(Admin, admin_id)
    if entity is None:
        raise EntityNotFoundException("Admin not found")
    return _to_admin_response(db, entity)


def update_admin(db: Session, admin_id: int, request: CreateAdminRequest, updater_id: int) -> AdminResponse:
    check_update_permission(db, updater_id, admin_id)
    entity = db.get(Admin, admin_id)
    if entity is None:
        raise EntityNotFoundException("Admin not found")
    account = entity.account
    account.name = _build_display_name(request.firstName, request.lastName)
    if not request.keepPassword and request.password:
        account.password = hash_password(request.password)
    _apply_org_fields(account, request)
    apply_reports_to(db, account, request.reportsToId)
    entity.first_name, entity.last_name = request.firstName, request.lastName
    entity.department, entity.phone = request.department, request.phone
    entity.employee_id, entity.qualification = request.employeeId, request.qualification
    entity.bio = request.bio
    if request.joinDate:
        entity.join_date = _parse_date(request.joinDate)
    _apply_hr_fields(entity, request)
    db.commit()
    return _to_admin_response(db, entity)


def delete_admin(db: Session, admin_id: int, deleter_id: int) -> None:
    check_delete_permission(db, deleter_id, admin_id)
    entity = db.get(Admin, admin_id)
    if entity is None:
        raise EntityNotFoundException("Admin not found")
    db.delete(entity)
    db.delete(entity.account)
    db.commit()


def get_all_admins(db: Session, requester_id: int) -> list[AdminResponse]:
    ensure_requester_can_list(db, requester_id, Role.ADMIN)
    return [_to_admin_response(db, e) for e in db.scalars(select(Admin))]


# ---------- Manager (identical shape to Admin) ----------


def _to_manager_response(db: Session, entity: Manager) -> AdminResponse:
    account = entity.account
    creator = db.get(Users, account.created_by_id) if account.created_by_id else None
    reports_to_name, reports_to_role = _resolve_reports_to(db, account)
    data = {f: (_fmt_date(getattr(entity, f)) if f in _DATE_FIELDS else getattr(entity, f)) for f in HR_PROFILE_FIELDS}
    return AdminResponse(
        id=entity.id, email=account.email, firstName=entity.first_name, lastName=entity.last_name,
        department=entity.department, phone=entity.phone, employeeId=entity.employee_id,
        qualification=entity.qualification, isActive=bool(account.is_active),
        createdByName=creator.name if creator else None, headOfficeId=account.head_office_id,
        branchId=account.branch_id, departmentId=account.department_id, teamId=account.team_id,
        designationId=account.designation_id, joinDate=entity.join_date, bio=entity.bio,
        reportsToId=account.reports_to_id, reportsToName=reports_to_name, reportsToRole=reports_to_role,
        createdAt=entity.created_at, updatedAt=entity.updated_at,
        **{_camel(k): v for k, v in data.items()},
    )


def create_manager(db: Session, request: CreateAdminRequest, creator_id: int) -> AdminResponse:
    creator = check_create_permission(db, creator_id, Role.MANAGER)
    account = create_account(db, request.email, request.password, request.firstName, request.lastName, Role.MANAGER, creator)
    _apply_org_fields(account, request)
    apply_reports_to(db, account, request.reportsToId)
    entity = Manager(
        id=account.id, first_name=request.firstName, last_name=request.lastName,
        department=request.department, phone=request.phone, employee_id=request.employeeId,
        qualification=request.qualification, join_date=_parse_date(request.joinDate) or datetime.now(),
        bio=request.bio,
    )
    _apply_hr_fields(entity, request)
    entity.account = account
    db.add(entity)
    db.commit()
    return _to_manager_response(db, entity)


def get_manager(db: Session, manager_id: int, requester_id: int) -> AdminResponse:
    check_view_permission(db, requester_id, manager_id)
    entity = db.get(Manager, manager_id)
    if entity is None:
        raise EntityNotFoundException("Manager not found")
    return _to_manager_response(db, entity)


def update_manager(db: Session, manager_id: int, request: CreateAdminRequest, updater_id: int) -> AdminResponse:
    check_update_permission(db, updater_id, manager_id)
    entity = db.get(Manager, manager_id)
    if entity is None:
        raise EntityNotFoundException("Manager not found")
    account = entity.account
    account.name = _build_display_name(request.firstName, request.lastName)
    if not request.keepPassword and request.password:
        account.password = hash_password(request.password)
    _apply_org_fields(account, request)
    apply_reports_to(db, account, request.reportsToId)
    entity.first_name, entity.last_name = request.firstName, request.lastName
    entity.department, entity.phone = request.department, request.phone
    entity.employee_id, entity.qualification = request.employeeId, request.qualification
    entity.bio = request.bio
    if request.joinDate:
        entity.join_date = _parse_date(request.joinDate)
    _apply_hr_fields(entity, request)
    db.commit()
    return _to_manager_response(db, entity)


def delete_manager(db: Session, manager_id: int, deleter_id: int) -> None:
    check_delete_permission(db, deleter_id, manager_id)
    entity = db.get(Manager, manager_id)
    if entity is None:
        raise EntityNotFoundException("Manager not found")
    db.delete(entity)
    db.delete(entity.account)
    db.commit()


def get_all_managers(db: Session, requester_id: int) -> list[AdminResponse]:
    ensure_requester_can_list(db, requester_id, Role.MANAGER)
    return [_to_manager_response(db, e) for e in db.scalars(select(Manager))]


# ---------- Trainer ----------


def _to_trainer_response(db: Session, entity: Trainer) -> TrainerResponse:
    account = entity.account
    creator = db.get(Users, account.created_by_id) if account.created_by_id else None
    reports_to_name, reports_to_role = _resolve_reports_to(db, account)
    data = {f: (_fmt_date(getattr(entity, f)) if f in _DATE_FIELDS else getattr(entity, f)) for f in HR_PROFILE_FIELDS}
    return TrainerResponse(
        id=entity.id, email=account.email, firstName=entity.first_name, lastName=entity.last_name,
        specialization=entity.specialization, experienceYears=entity.experience_years,
        certification=entity.certification, phone=entity.phone, qualification=entity.qualification,
        ratePerHour=entity.rate_per_hour, isActive=bool(account.is_active),
        createdByName=creator.name if creator else None, headOfficeId=account.head_office_id,
        branchId=account.branch_id, departmentId=account.department_id, teamId=account.team_id,
        designationId=account.designation_id, joinDate=entity.join_date, bio=entity.bio,
        languages=entity.languages, rating=entity.rating, totalClientsTrained=entity.total_clients_trained,
        reportsToId=account.reports_to_id, reportsToName=reports_to_name, reportsToRole=reports_to_role,
        createdAt=entity.created_at, updatedAt=entity.updated_at,
        **{_camel(k): v for k, v in data.items()},
    )


def create_trainer(db: Session, request: CreateTrainerRequest, creator_id: int) -> TrainerResponse:
    creator = check_create_permission(db, creator_id, Role.TRAINER)
    account = create_account(db, request.email, request.password, request.firstName, request.lastName, Role.TRAINER, creator)
    _apply_org_fields(account, request)
    apply_reports_to(db, account, request.reportsToId)
    entity = Trainer(
        id=account.id, first_name=request.firstName, last_name=request.lastName,
        specialization=request.specialization, experience_years=request.experienceYears,
        certification=request.certification, phone=request.phone, qualification=request.qualification,
        rate_per_hour=request.ratePerHour, join_date=_parse_date(request.joinDate) or datetime.now(),
        bio=request.bio, languages=request.languages, rating=request.rating or 0.0,
        total_clients_trained=request.totalClientsTrained or 0,
    )
    _apply_hr_fields(entity, request)
    entity.account = account
    db.add(entity)
    db.commit()
    return _to_trainer_response(db, entity)


def get_trainer(db: Session, trainer_id: int, requester_id: int) -> TrainerResponse:
    check_view_permission(db, requester_id, trainer_id)
    entity = db.get(Trainer, trainer_id)
    if entity is None:
        raise EntityNotFoundException("Trainer not found")
    return _to_trainer_response(db, entity)


def update_trainer(db: Session, trainer_id: int, request: CreateTrainerRequest, updater_id: int) -> TrainerResponse:
    check_update_permission(db, updater_id, trainer_id)
    entity = db.get(Trainer, trainer_id)
    if entity is None:
        raise EntityNotFoundException("Trainer not found")
    account = entity.account
    account.name = _build_display_name(request.firstName, request.lastName)
    if not request.keepPassword and request.password:
        account.password = hash_password(request.password)
    _apply_org_fields(account, request)
    apply_reports_to(db, account, request.reportsToId)
    entity.first_name, entity.last_name = request.firstName, request.lastName
    entity.specialization, entity.experience_years = request.specialization, request.experienceYears
    entity.certification, entity.phone = request.certification, request.phone
    entity.qualification, entity.rate_per_hour = request.qualification, request.ratePerHour
    entity.bio, entity.languages = request.bio, request.languages
    if request.rating is not None:
        entity.rating = request.rating
    if request.totalClientsTrained is not None:
        entity.total_clients_trained = request.totalClientsTrained
    if request.joinDate:
        entity.join_date = _parse_date(request.joinDate)
    _apply_hr_fields(entity, request)
    db.commit()
    return _to_trainer_response(db, entity)


def delete_trainer(db: Session, trainer_id: int, deleter_id: int) -> None:
    check_delete_permission(db, deleter_id, trainer_id)
    entity = db.get(Trainer, trainer_id)
    if entity is None:
        raise EntityNotFoundException("Trainer not found")
    db.delete(entity)
    db.delete(entity.account)
    db.commit()


def get_all_trainers(db: Session, requester_id: int) -> list[TrainerResponse]:
    _get_user_or_404(db, requester_id)
    return [_to_trainer_response(db, e) for e in db.scalars(select(Trainer))]


# ---------- Counselor ----------


def _to_counselor_response(entity: Counselor) -> CounselorResponse:
    return CounselorResponse(
        id=entity.id, email=entity.account.email, firstName=entity.first_name, lastName=entity.last_name,
        phone=entity.phone, isActive=bool(entity.account.is_active), joinDate=entity.join_date,
        createdAt=entity.created_at, updatedAt=entity.updated_at,
    )


def create_counselor(db: Session, request: CreateCounselorRequest, creator_id: int) -> CounselorResponse:
    creator = check_create_permission(db, creator_id, Role.COUNSELOR)
    account = create_account(db, request.email, request.password, request.firstName, request.lastName, Role.COUNSELOR, creator)
    entity = Counselor(id=account.id, first_name=request.firstName, last_name=request.lastName, phone=request.phone)
    entity.account = account
    db.add(entity)
    db.commit()
    return _to_counselor_response(entity)


def get_counselor(db: Session, counselor_id: int, requester_id: int) -> CounselorResponse:
    check_view_permission(db, requester_id, counselor_id)
    entity = db.get(Counselor, counselor_id)
    if entity is None:
        raise EntityNotFoundException("Counselor not found")
    return _to_counselor_response(entity)


def update_counselor(db: Session, counselor_id: int, request: CreateCounselorRequest, updater_id: int) -> CounselorResponse:
    check_update_permission(db, updater_id, counselor_id)
    entity = db.get(Counselor, counselor_id)
    if entity is None:
        raise EntityNotFoundException("Counselor not found")
    entity.account.name = _build_display_name(request.firstName, request.lastName)
    if not request.keepPassword and request.password:
        entity.account.password = hash_password(request.password)
    entity.first_name, entity.last_name, entity.phone = request.firstName, request.lastName, request.phone
    db.commit()
    return _to_counselor_response(entity)


def delete_counselor(db: Session, counselor_id: int, deleter_id: int) -> None:
    check_delete_permission(db, deleter_id, counselor_id)
    entity = db.get(Counselor, counselor_id)
    if entity is None:
        raise EntityNotFoundException("Counselor not found")
    db.delete(entity)
    db.delete(entity.account)
    db.commit()


def get_all_counselors(db: Session, requester_id: int) -> list[CounselorResponse]:
    ensure_requester_can_list(db, requester_id, Role.COUNSELOR)
    return [_to_counselor_response(e) for e in db.scalars(select(Counselor))]


# ---------- SuperAdmin ----------


def _to_super_admin_response(entity: SuperAdmin) -> SuperAdminResponse:
    return SuperAdminResponse(
        id=entity.id, email=entity.account.email, firstName=entity.first_name, lastName=entity.last_name,
        isActive=bool(entity.account.is_active),
    )


def create_super_admin(db: Session, request: CreateSuperAdminRequest, creator_id: int) -> SuperAdminResponse:
    creator = check_create_permission(db, creator_id, Role.SUPER_ADMIN)
    account = create_account(db, request.email, request.password, request.firstName, request.lastName, Role.SUPER_ADMIN, creator)
    entity = SuperAdmin(id=account.id, first_name=request.firstName, last_name=request.lastName)
    entity.account = account
    db.add(entity)
    db.commit()
    return _to_super_admin_response(entity)


def get_super_admin(db: Session, super_admin_id: int, requester_id: int) -> SuperAdminResponse:
    check_view_permission(db, requester_id, super_admin_id)
    entity = db.get(SuperAdmin, super_admin_id)
    if entity is None:
        raise ForbiddenException("Super admin not found")
    return _to_super_admin_response(entity)


def update_super_admin(db: Session, super_admin_id: int, request: CreateSuperAdminRequest, updater_id: int) -> SuperAdminResponse:
    check_update_permission(db, updater_id, super_admin_id)
    entity = db.get(SuperAdmin, super_admin_id)
    if entity is None:
        raise ForbiddenException("Super admin not found")
    entity.account.name = _build_display_name(request.firstName, request.lastName)
    if not request.keepPassword and request.password:
        entity.account.password = hash_password(request.password)
    entity.first_name, entity.last_name = request.firstName, request.lastName
    db.commit()
    return _to_super_admin_response(entity)


def delete_super_admin(db: Session, super_admin_id: int, deleter_id: int) -> None:
    check_delete_permission(db, deleter_id, super_admin_id)
    entity = db.get(SuperAdmin, super_admin_id)
    if entity is None:
        raise ForbiddenException("Super admin not found")
    db.delete(entity)
    db.delete(entity.account)
    db.commit()


def get_all_super_admins(db: Session, requester_id: int) -> list[SuperAdminResponse]:
    ensure_requester_can_list(db, requester_id, Role.SUPER_ADMIN)
    return [_to_super_admin_response(e) for e in db.scalars(select(SuperAdmin))]


# ---------- Corporate HR (no dedicated profile entity — Users row with role=CORPORATE_HR) ----------


def _to_corporate_hr_response(account: Users) -> CorporateHrResponse:
    return CorporateHrResponse(
        id=account.id, companyName=None, email=account.email, role=Role(account.role),
        active=bool(account.is_active), createdAt=account.created_at, updatedAt=account.updated_at,
    )


def create_corporate_hr(db: Session, request: CreateCorporateHrRequest, creator_id: int) -> CorporateHrResponse:
    creator = check_create_permission(db, creator_id, Role.CORPORATE_HR)
    account = create_account(db, request.email, request.password, request.companyName, None, Role.CORPORATE_HR, creator)
    db.commit()
    return _to_corporate_hr_response(account)


def update_corporate_hr(db: Session, id_: int, request: CreateCorporateHrRequest, updater_id: int) -> CorporateHrResponse:
    check_update_permission(db, updater_id, id_)
    account = db.get(Users, id_)
    if account is None or Role(account.role) != Role.CORPORATE_HR:
        raise EntityNotFoundException("Corporate HR account not found")
    account.name = request.companyName
    if not request.keepPassword and request.password:
        account.password = hash_password(request.password)
    db.commit()
    return _to_corporate_hr_response(account)


def delete_corporate_hr(db: Session, id_: int, deleter_id: int) -> None:
    check_delete_permission(db, deleter_id, id_)
    account = db.get(Users, id_)
    if account is None or Role(account.role) != Role.CORPORATE_HR:
        raise EntityNotFoundException("Corporate HR account not found")
    db.delete(account)
    db.commit()


def get_all_corporate_hr(db: Session, requester_id: int) -> list[CorporateHrResponse]:
    ensure_requester_can_list(db, requester_id, Role.CORPORATE_HR)
    return [_to_corporate_hr_response(a) for a in db.scalars(select(Users).where(Users.role == Role.CORPORATE_HR))]


# ---------- Customers (FitnessUser) ----------


def _to_customer_response(db: Session, fu: FitnessUser) -> CustomerResponse:
    account = fu.account
    trainer = db.get(Trainer, fu.assigned_trainer_id) if fu.assigned_trainer_id else None
    membership_plan_name = "BASIC"
    if fu.membership_plan_id:
        from app.models.membership import MembershipPlan

        plan = db.get(MembershipPlan, fu.membership_plan_id)
        if plan is not None:
            membership_plan_name = plan.name
    elif fu.membership_plan:
        membership_plan_name = fu.membership_plan
    return CustomerResponse(
        id=fu.id, email=account.email, firstName=fu.first_name, lastName=fu.last_name,
        headOfficeId=account.head_office_id, branchId=account.branch_id, departmentId=account.department_id,
        teamId=account.team_id, designationId=account.designation_id, weight=fu.weight, height=fu.height,
        bloodGroup=fu.blood_group, age=fu.age, gender=fu.gender, phone=fu.phone, address=fu.address,
        city=fu.city, medicalConditions=fu.medical_conditions, emergencyContact=fu.emergency_contact,
        emergencyPhone=fu.emergency_phone, isActive=bool(account.is_active), isApproved=bool(account.is_approved),
        assignedTrainerId=fu.assigned_trainer_id, assignedTrainerName=trainer.first_name if trainer else None,
        trainerId=account.trainer_id, createdById=account.created_by_id, photoPath=fu.photo_path,
        idProofPath=fu.id_proof_path, assignedDietPlanId=account.assigned_diet_plan_id, assignedDietPlanName=None,
        assignedWorkoutPlanId=account.assigned_workout_plan_id, assignedWorkoutPlanName=None,
        bodyFat=fu.body_fat, isFrozen=bool(fu.is_frozen), referredBy=fu.referred_by,
        membershipExpiry=_fmt_date(fu.membership_expiry), membershipPlan=membership_plan_name,
        membershipPlanId=fu.membership_plan_id,
        accessStartTime=fu.access_start_time.strftime("%H:%M") if fu.access_start_time else None,
        accessEndTime=fu.access_end_time.strftime("%H:%M") if fu.access_end_time else None,
    )


def create_customer_self(db: Session, request: CreateCustomerSelfRequest) -> CustomerResponse:
    account = create_account(db, request.email, request.password, request.firstName, request.lastName, Role.USER, None)
    account.is_active = False
    account.is_approved = False
    fu = FitnessUser(
        id=account.id, first_name=request.firstName, last_name=request.lastName,
        details_completed=False, registration_date=datetime.now(),
    )
    fu.account = account
    db.add(fu)
    db.commit()
    return _to_customer_response(db, fu)


def create_customer_by_trainer(db: Session, request: CreateCustomerByTrainerRequest, trainer_id: int) -> CustomerResponse:
    creator = check_create_permission(db, trainer_id, Role.USER)
    if request.assignedTrainerId is None:
        raise ValueError("assignedTrainerId is required")
    if Role(creator.role) != Role.TRAINER:
        raise ValueError("Only a trainer can create a customer via this endpoint")
    trainer_entity = db.get(Trainer, request.assignedTrainerId)
    if trainer_entity is None:
        raise EntityNotFoundException("Assigned trainer not found")

    account = create_account(db, request.email, request.password, request.firstName, request.lastName, Role.USER, creator)
    account.team_id = request.teamId
    account.trainer_id = trainer_entity.id
    account.manager_id = trainer_entity.account.manager_id
    fu = FitnessUser(
        id=account.id, first_name=request.firstName, last_name=request.lastName, weight=request.weight,
        height=request.height, blood_group=request.bloodGroup, age=request.age, gender=request.gender,
        phone=request.phone, address=request.address, city=request.city,
        medical_conditions=request.medicalConditions, emergency_contact=request.emergencyContact,
        emergency_phone=request.emergencyPhone, assigned_trainer_id=trainer_entity.id,
        details_completed=True, registration_date=datetime.now(), photo_path=request.photoPath,
        id_proof_path=request.idProofPath, body_fat=request.bodyFat, is_frozen=bool(request.isFrozen),
        referred_by=request.referredBy,
    )
    fu.account = account
    db.add(fu)
    db.commit()
    return _to_customer_response(db, fu)


def get_customer(db: Session, customer_id: int, requester_id: int) -> CustomerResponse:
    check_view_permission(db, requester_id, customer_id)
    fu = db.get(FitnessUser, customer_id)
    if fu is None:
        raise EntityNotFoundException("Customer not found")
    return _to_customer_response(db, fu)


def update_customer(db: Session, customer_id: int, request: UpdateCustomerDetailsRequest, updater_id: int) -> CustomerResponse:
    updater = _get_user_or_404(db, updater_id)
    if not (Role(updater.role) == Role.USER and updater_id == customer_id):
        check_update_permission(db, updater_id, customer_id)
    if request.assignedTrainerId is None:
        raise ValueError("assignedTrainerId is required")

    fu = db.get(FitnessUser, customer_id)
    if fu is None:
        raise EntityNotFoundException("Customer not found")
    account = fu.account
    if request.firstName is not None:
        fu.first_name = request.firstName
    if request.lastName is not None:
        fu.last_name = request.lastName
        account.name = _build_display_name(fu.first_name, fu.last_name)
    if request.password:
        account.password = hash_password(request.password)
    account.head_office_id = request.headOfficeId
    account.branch_id = request.branchId
    account.department_id = request.departmentId
    account.team_id = request.teamId
    account.designation_id = request.designationId
    account.trainer_id = request.assignedTrainerId

    fu.weight, fu.height, fu.blood_group = request.weight, request.height, request.bloodGroup
    fu.age, fu.gender, fu.phone = request.age, request.gender, request.phone
    fu.address, fu.city = request.address, request.city
    fu.medical_conditions = request.medicalConditions
    fu.emergency_contact, fu.emergency_phone = request.emergencyContact, request.emergencyPhone
    fu.assigned_trainer_id = request.assignedTrainerId
    if request.photoPath:
        fu.photo_path = request.photoPath
    if request.idProofPath:
        fu.id_proof_path = request.idProofPath
    if request.bodyFat is not None:
        fu.body_fat = request.bodyFat
    if request.isFrozen is not None:
        fu.is_frozen = request.isFrozen
    if request.referredBy is not None:
        fu.referred_by = request.referredBy

    db.commit()
    return _to_customer_response(db, fu)


def delete_customer(db: Session, customer_id: int, deleter_id: int) -> None:
    check_delete_permission(db, deleter_id, customer_id)
    fu = db.get(FitnessUser, customer_id)
    if fu is None:
        raise EntityNotFoundException("Customer not found")

    from sqlalchemy import delete as sa_delete

    from app.models.attendance import Attendance
    from app.models.chat import ChatMessage
    from app.models.fitness import Goal, ProgressEntry, UserWorkoutSchedule
    from app.models.membership import MembershipRequest
    from app.models.notification import Notification

    db.execute(sa_delete(Attendance).where(Attendance.user_id == customer_id))
    db.execute(sa_delete(Goal).where(Goal.user_id == customer_id))
    db.execute(sa_delete(ProgressEntry).where(ProgressEntry.user_id == customer_id))
    db.execute(sa_delete(ChatMessage).where(ChatMessage.member_id == customer_id))
    db.execute(sa_delete(Notification).where(Notification.recipient_id == customer_id))
    db.execute(sa_delete(UserWorkoutSchedule).where(UserWorkoutSchedule.user_id == customer_id))
    db.execute(sa_delete(MembershipRequest).where(MembershipRequest.member_id == customer_id))

    db.delete(fu)
    db.delete(fu.account)
    db.commit()


def get_visible_customers_for_requester(db: Session, requester: Users) -> list[FitnessUser]:
    role = Role(requester.role)
    if role == Role.SUPER_ADMIN:
        return list(db.scalars(select(FitnessUser)))
    if role == Role.ADMIN:
        return list(
            db.scalars(select(FitnessUser).join(Users, FitnessUser.id == Users.id).where(Users.admin_id == requester.id))
        )
    if role == Role.MANAGER:
        return list(
            db.scalars(select(FitnessUser).join(Users, FitnessUser.id == Users.id).where(Users.manager_id == requester.id))
        )
    if role == Role.TRAINER:
        by_assigned = set(db.scalars(select(FitnessUser.id).where(FitnessUser.assigned_trainer_id == requester.id)))
        by_trainer = set(
            db.scalars(select(FitnessUser.id).join(Users, FitnessUser.id == Users.id).where(Users.trainer_id == requester.id))
        )
        by_created = set(
            db.scalars(select(FitnessUser.id).join(Users, FitnessUser.id == Users.id).where(Users.created_by_id == requester.id))
        )
        ids = by_assigned | by_trainer | by_created
        return list(db.scalars(select(FitnessUser).where(FitnessUser.id.in_(ids)))) if ids else []
    raise ForbiddenException("Not permitted to list customers")


def get_all_customers(db: Session, requester_id: int) -> list[CustomerResponse]:
    requester = _get_user_or_404(db, requester_id)
    return [_to_customer_response(db, fu) for fu in get_visible_customers_for_requester(db, requester)]


def get_customers_assigned_to_trainer(db: Session, trainer_id: int) -> list[CustomerResponse]:
    rows = db.scalars(select(FitnessUser).where(FitnessUser.assigned_trainer_id == trainer_id))
    return [_to_customer_response(db, fu) for fu in rows]


def activate_customer(db: Session, customer_id: int, trainer_id: int) -> None:
    trainer_account = _get_user_or_404(db, trainer_id)
    if Role(trainer_account.role) != Role.TRAINER:
        raise ForbiddenException("Only a trainer can activate a customer")
    fu = db.get(FitnessUser, customer_id)
    if fu is None:
        raise EntityNotFoundException("Customer not found")
    account = fu.account
    account.is_active = True
    account.is_approved = True
    account.admin_id = trainer_account.admin_id
    account.manager_id = trainer_account.manager_id
    account.trainer_id = trainer_account.id
    trainer_entity = db.get(Trainer, trainer_id)
    if trainer_entity is not None:
        fu.assigned_trainer = trainer_entity
    db.commit()


def set_user_active_status(db: Session, user_id: int, active: bool, updater_id: int) -> None:
    check_update_permission(db, updater_id, user_id)
    account = _get_user_or_404(db, user_id)
    account.is_active = active
    if active and Role(account.role) == Role.USER:
        account.is_approved = True
    db.commit()


# ---------- Profile ----------


def get_my_profile(db: Session, user: Users) -> MyProfileResponse:
    fu = db.get(FitnessUser, user.id)
    return MyProfileResponse(
        id=user.id, name=user.name, email=user.email, role=str(user.role),
        weight=fu.weight if fu else None, height=fu.height if fu else None, age=fu.age if fu else None,
        gender=fu.gender if fu else None, dateOfBirth=_fmt_date(fu.date_of_birth) if fu and hasattr(fu, "date_of_birth") else None,
        bloodGroup=fu.blood_group if fu else None, phone=fu.phone if fu else None, address=fu.address if fu else None,
        city=fu.city if fu else None, bio=fu.bio if fu else None, fitnessGoals=fu.fitness_goals if fu else None,
        hasFitnessProfile=fu is not None,
    )


def update_my_profile(db: Session, user: Users, request: MyProfileRequest) -> MyProfileResponse:
    if request.name:
        user.name = request.name
    fu = db.get(FitnessUser, user.id)
    if fu is None and Role(user.role) == Role.USER:
        fu = FitnessUser(id=user.id, details_completed=False, registration_date=datetime.now())
        fu.account = user
        db.add(fu)
    if fu is not None:
        if request.weight is not None:
            fu.weight = request.weight
        if request.height is not None:
            fu.height = request.height
        if request.age is not None:
            fu.age = request.age
        if request.gender is not None:
            fu.gender = request.gender
        if request.bloodGroup is not None:
            fu.blood_group = request.bloodGroup
        if request.phone is not None:
            fu.phone = request.phone
        if request.address is not None:
            fu.address = request.address
        if request.city is not None:
            fu.city = request.city
        if request.bio is not None:
            fu.bio = request.bio
        if request.fitnessGoals is not None:
            fu.fitness_goals = request.fitnessGoals
    db.commit()
    return get_my_profile(db, user)


def complete_onboarding(db: Session, user: Users) -> None:
    user.onboarding_completed_at = datetime.now()
    db.commit()


# ---------- Expiring / at-risk members ----------


def get_expiring_members(db: Session, days: int, requester_id: int) -> list[CustomerResponse]:
    requester = _get_user_or_404(db, requester_id)
    today = date.today()
    horizon = today + timedelta(days=days)
    visible = get_visible_customers_for_requester(db, requester)
    expiring = [
        fu for fu in visible
        if fu.membership_expiry is not None and today <= fu.membership_expiry <= horizon
    ]
    expiring.sort(key=lambda fu: fu.membership_expiry)
    return [_to_customer_response(db, fu) for fu in expiring]


def get_at_risk_members(db: Session, requester_id: int) -> list[AtRiskMemberResponse]:
    from app.models.attendance import Attendance

    requester = _get_user_or_404(db, requester_id)
    today = date.today()
    result = []
    for fu in get_visible_customers_for_requester(db, requester):
        reasons: list[str] = []
        last_visit = db.scalar(
            select(Attendance.check_in_time)
            .where(Attendance.user_id == fu.id)
            .order_by(Attendance.check_in_time.desc())
            .limit(1)
        )
        if last_visit is None:
            reasons.append("Never visited")
        elif (datetime.now() - last_visit).days >= 10:
            reasons.append("Not visited in 10 days")

        if fu.membership_expiry is not None:
            if fu.membership_expiry < today:
                reasons.append("Membership expired")
            elif (fu.membership_expiry - today).days <= 15:
                reasons.append("Membership ending soon")

        result.append(
            AtRiskMemberResponse(
                id=fu.id, firstName=fu.first_name, lastName=fu.last_name, email=fu.account.email,
                phone=fu.phone, reasons=reasons,
            )
        )
    return result


# ---------- Reporting options ----------


def get_reporting_options(db: Session, target_role: Role, branch_id: int | None, requester_id: int) -> list[ReportingOptionResponse]:
    requester = _get_user_or_404(db, requester_id)
    if _role_level(target_role) >= _role_level(requester.role):
        raise ForbiddenException("Not permitted to view reporting options for this role")
    allowed = _ALLOWED_REPORTS_TO.get(target_role, set())
    stmt = select(Users).where(Users.role.in_([r.value for r in allowed]))
    if branch_id is not None:
        stmt = stmt.where(Users.branch_id == branch_id)
    rows = [u for u in db.scalars(stmt) if u.id != requester_id]
    return [ReportingOptionResponse(id=u.id, name=u.name, email=u.email, role=Role(u.role), branchId=u.branch_id) for u in rows]
