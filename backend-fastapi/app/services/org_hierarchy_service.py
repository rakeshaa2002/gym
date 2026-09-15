from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.core.exceptions import DuplicateResourceException, EntityNotFoundException, InvalidOperationException
from app.models.org import Branch, Department, Designation, HeadOffice, Team
from app.schemas.org import (
    BranchRequest,
    BranchResponse,
    DepartmentRequest,
    DepartmentResponse,
    DesignationRequest,
    DesignationResponse,
    HeadOfficeRequest,
    HeadOfficeResponse,
    TeamRequest,
    TeamResponse,
)


def _require_name(value: str | None) -> str:
    v = (value or "").strip()
    if not v:
        raise ValueError("Name is required")
    return v


def _trim_to_none(value: str | None) -> str | None:
    if value is None:
        return None
    v = value.strip()
    return v or None


def _normalize_status(status: str | None) -> str:
    if not status or not status.strip():
        return "ACTIVE"
    s = status.strip().upper()
    if s not in ("ACTIVE", "INACTIVE"):
        raise ValueError("Status must be ACTIVE or INACTIVE")
    return s


# ---------- Head Office ----------


def _to_head_office_response(o: HeadOffice) -> HeadOfficeResponse:
    return HeadOfficeResponse(
        id=o.id, name=o.name, location=o.location, address=o.address, phone=o.phone, email=o.email,
        status=o.status, createdAt=o.created_at, updatedAt=o.updated_at,
    )


def get_all_head_offices(db: Session) -> list[HeadOfficeResponse]:
    return [_to_head_office_response(o) for o in db.scalars(select(HeadOffice))]


def create_head_office(db: Session, request: HeadOfficeRequest) -> HeadOfficeResponse:
    name = _require_name(request.name)
    if db.scalar(select(HeadOffice).where(HeadOffice.name == name)) is not None:
        raise DuplicateResourceException(f"Head office '{name}' already exists")
    entity = HeadOffice(
        name=name, location=_trim_to_none(request.location), address=_trim_to_none(request.address),
        phone=_trim_to_none(request.phone), email=_trim_to_none(request.email),
        status=_normalize_status(request.status),
    )
    db.add(entity)
    db.commit()
    return _to_head_office_response(entity)


def update_head_office(db: Session, id_: int, request: HeadOfficeRequest) -> HeadOfficeResponse:
    entity = db.get(HeadOffice, id_)
    if entity is None:
        raise EntityNotFoundException(f"Head office {id_} not found")
    name = _require_name(request.name)
    existing = db.scalar(select(HeadOffice).where(HeadOffice.name == name))
    if existing is not None and existing.id != id_:
        raise DuplicateResourceException(f"Head office '{name}' already exists")
    entity.name = name
    entity.location = _trim_to_none(request.location)
    entity.address = _trim_to_none(request.address)
    entity.phone = _trim_to_none(request.phone)
    entity.email = _trim_to_none(request.email)
    entity.status = _normalize_status(request.status)
    db.commit()
    return _to_head_office_response(entity)


def delete_head_office(db: Session, id_: int) -> None:
    entity = db.get(HeadOffice, id_)
    if entity is None:
        raise EntityNotFoundException(f"Head office {id_} not found")
    count = db.scalar(select(func.count()).select_from(Branch).where(Branch.head_office_id == id_))
    if count and count > 0:
        raise InvalidOperationException("Cannot delete head office with linked branches")
    db.delete(entity)
    db.commit()


# ---------- Branch ----------


def _to_branch_response(o: Branch) -> BranchResponse:
    return BranchResponse(
        id=o.id, name=o.name, headOfficeId=o.head_office_id, location=o.location, address=o.address,
        phone=o.phone, email=o.email, managerName=o.manager_name, status=o.status,
        createdAt=o.created_at, updatedAt=o.updated_at,
    )


def get_all_branches(db: Session) -> list[BranchResponse]:
    return [_to_branch_response(o) for o in db.scalars(select(Branch))]


def get_branches_by_head_office(db: Session, head_office_id: int) -> list[BranchResponse]:
    return [_to_branch_response(o) for o in db.scalars(select(Branch).where(Branch.head_office_id == head_office_id))]


def create_branch(db: Session, request: BranchRequest) -> BranchResponse:
    name = _require_name(request.name)
    if db.get(HeadOffice, request.headOfficeId) is None:
        raise EntityNotFoundException(f"Head office {request.headOfficeId} not found")
    dup = db.scalar(
        select(Branch).where(Branch.head_office_id == request.headOfficeId, Branch.name == name)
    )
    if dup is not None:
        raise DuplicateResourceException(f"Branch '{name}' already exists in this head office")
    entity = Branch(
        name=name, head_office_id=request.headOfficeId, location=_trim_to_none(request.location),
        address=_trim_to_none(request.address), phone=_trim_to_none(request.phone),
        email=_trim_to_none(request.email), manager_name=_trim_to_none(request.managerName),
        status=_normalize_status(request.status),
    )
    db.add(entity)
    db.commit()
    return _to_branch_response(entity)


def update_branch(db: Session, id_: int, request: BranchRequest) -> BranchResponse:
    entity = db.get(Branch, id_)
    if entity is None:
        raise EntityNotFoundException(f"Branch {id_} not found")
    name = _require_name(request.name)
    if db.get(HeadOffice, request.headOfficeId) is None:
        raise EntityNotFoundException(f"Head office {request.headOfficeId} not found")
    dup = db.scalar(
        select(Branch).where(Branch.head_office_id == request.headOfficeId, Branch.name == name)
    )
    if dup is not None and dup.id != id_:
        raise DuplicateResourceException(f"Branch '{name}' already exists in this head office")
    entity.name = name
    entity.head_office_id = request.headOfficeId
    entity.location = _trim_to_none(request.location)
    entity.address = _trim_to_none(request.address)
    entity.phone = _trim_to_none(request.phone)
    entity.email = _trim_to_none(request.email)
    entity.manager_name = _trim_to_none(request.managerName)
    entity.status = _normalize_status(request.status)
    db.commit()
    return _to_branch_response(entity)


def delete_branch(db: Session, id_: int) -> None:
    entity = db.get(Branch, id_)
    if entity is None:
        raise EntityNotFoundException(f"Branch {id_} not found")
    count = db.scalar(select(func.count()).select_from(Department).where(Department.branch_id == id_))
    if count and count > 0:
        raise InvalidOperationException("Cannot delete branch with linked departments")
    db.delete(entity)
    db.commit()


# ---------- Department ----------


def _to_department_response(o: Department) -> DepartmentResponse:
    return DepartmentResponse(
        id=o.id, name=o.name, branchId=o.branch_id, description=o.description, status=o.status,
        createdAt=o.created_at, updatedAt=o.updated_at,
    )


def get_all_departments(db: Session) -> list[DepartmentResponse]:
    return [_to_department_response(o) for o in db.scalars(select(Department))]


def get_departments_by_branch(db: Session, branch_id: int) -> list[DepartmentResponse]:
    return [_to_department_response(o) for o in db.scalars(select(Department).where(Department.branch_id == branch_id))]


def create_department(db: Session, request: DepartmentRequest) -> DepartmentResponse:
    name = _require_name(request.name)
    if db.get(Branch, request.branchId) is None:
        raise EntityNotFoundException(f"Branch {request.branchId} not found")
    dup = db.scalar(select(Department).where(Department.branch_id == request.branchId, Department.name == name))
    if dup is not None:
        raise DuplicateResourceException(f"Department '{name}' already exists in this branch")
    entity = Department(
        name=name, branch_id=request.branchId, description=_trim_to_none(request.description),
        status=_normalize_status(request.status),
    )
    db.add(entity)
    db.commit()
    return _to_department_response(entity)


def update_department(db: Session, id_: int, request: DepartmentRequest) -> DepartmentResponse:
    entity = db.get(Department, id_)
    if entity is None:
        raise EntityNotFoundException(f"Department {id_} not found")
    name = _require_name(request.name)
    if db.get(Branch, request.branchId) is None:
        raise EntityNotFoundException(f"Branch {request.branchId} not found")
    dup = db.scalar(select(Department).where(Department.branch_id == request.branchId, Department.name == name))
    if dup is not None and dup.id != id_:
        raise DuplicateResourceException(f"Department '{name}' already exists in this branch")
    entity.name = name
    entity.branch_id = request.branchId
    entity.description = _trim_to_none(request.description)
    entity.status = _normalize_status(request.status)
    db.commit()
    return _to_department_response(entity)


def delete_department(db: Session, id_: int) -> None:
    entity = db.get(Department, id_)
    if entity is None:
        raise EntityNotFoundException(f"Department {id_} not found")
    team_count = db.scalar(select(func.count()).select_from(Team).where(Team.department_id == id_))
    designation_count = db.scalar(select(func.count()).select_from(Designation).where(Designation.department_id == id_))
    if (team_count and team_count > 0) or (designation_count and designation_count > 0):
        raise InvalidOperationException("Cannot delete department with linked teams/designations")
    db.delete(entity)
    db.commit()


# ---------- Team ----------


def _to_team_response(o: Team) -> TeamResponse:
    return TeamResponse(
        id=o.id, name=o.name, departmentId=o.department_id, description=o.description, teamLead=o.team_lead,
        memberCount=o.member_count, status=o.status, createdAt=o.created_at, updatedAt=o.updated_at,
    )


def get_all_teams(db: Session) -> list[TeamResponse]:
    return [_to_team_response(o) for o in db.scalars(select(Team))]


def get_teams_by_department(db: Session, department_id: int) -> list[TeamResponse]:
    return [_to_team_response(o) for o in db.scalars(select(Team).where(Team.department_id == department_id))]


def create_team(db: Session, request: TeamRequest) -> TeamResponse:
    name = _require_name(request.name)
    if db.get(Department, request.departmentId) is None:
        raise EntityNotFoundException(f"Department {request.departmentId} not found")
    dup = db.scalar(select(Team).where(Team.department_id == request.departmentId, Team.name == name))
    if dup is not None:
        raise DuplicateResourceException(f"Team '{name}' already exists in this department")
    entity = Team(
        name=name, department_id=request.departmentId, description=_trim_to_none(request.description),
        team_lead=_trim_to_none(request.teamLead), member_count=max(request.memberCount or 0, 0),
        status=_normalize_status(request.status),
    )
    db.add(entity)
    db.commit()
    return _to_team_response(entity)


def update_team(db: Session, id_: int, request: TeamRequest) -> TeamResponse:
    entity = db.get(Team, id_)
    if entity is None:
        raise EntityNotFoundException(f"Team {id_} not found")
    name = _require_name(request.name)
    if db.get(Department, request.departmentId) is None:
        raise EntityNotFoundException(f"Department {request.departmentId} not found")
    dup = db.scalar(select(Team).where(Team.department_id == request.departmentId, Team.name == name))
    if dup is not None and dup.id != id_:
        raise DuplicateResourceException(f"Team '{name}' already exists in this department")
    entity.name = name
    entity.department_id = request.departmentId
    entity.description = _trim_to_none(request.description)
    entity.team_lead = _trim_to_none(request.teamLead)
    entity.member_count = max(request.memberCount or 0, 0)
    entity.status = _normalize_status(request.status)
    db.commit()
    return _to_team_response(entity)


def delete_team(db: Session, id_: int) -> None:
    entity = db.get(Team, id_)
    if entity is None:
        raise EntityNotFoundException(f"Team {id_} not found")
    db.delete(entity)
    db.commit()


# ---------- Designation ----------


def _to_designation_response(o: Designation) -> DesignationResponse:
    return DesignationResponse(
        id=o.id, name=o.name, departmentId=o.department_id, description=o.description, level=o.level,
        salary=o.salary, status=o.status, createdAt=o.created_at, updatedAt=o.updated_at,
    )


def get_all_designations(db: Session) -> list[DesignationResponse]:
    return [_to_designation_response(o) for o in db.scalars(select(Designation))]


def get_designations_by_department(db: Session, department_id: int) -> list[DesignationResponse]:
    return [
        _to_designation_response(o)
        for o in db.scalars(select(Designation).where(Designation.department_id == department_id))
    ]


def create_designation(db: Session, request: DesignationRequest) -> DesignationResponse:
    name = _require_name(request.name)
    if db.get(Department, request.departmentId) is None:
        raise EntityNotFoundException(f"Department {request.departmentId} not found")
    dup = db.scalar(select(Designation).where(Designation.department_id == request.departmentId, Designation.name == name))
    if dup is not None:
        raise DuplicateResourceException(f"Designation '{name}' already exists in this department")
    entity = Designation(
        name=name, department_id=request.departmentId, description=_trim_to_none(request.description),
        level=_trim_to_none(request.level), salary=max(request.salary or 0, 0),
        status=_normalize_status(request.status),
    )
    db.add(entity)
    db.commit()
    return _to_designation_response(entity)


def update_designation(db: Session, id_: int, request: DesignationRequest) -> DesignationResponse:
    entity = db.get(Designation, id_)
    if entity is None:
        raise EntityNotFoundException(f"Designation {id_} not found")
    name = _require_name(request.name)
    if db.get(Department, request.departmentId) is None:
        raise EntityNotFoundException(f"Department {request.departmentId} not found")
    dup = db.scalar(select(Designation).where(Designation.department_id == request.departmentId, Designation.name == name))
    if dup is not None and dup.id != id_:
        raise DuplicateResourceException(f"Designation '{name}' already exists in this department")
    entity.name = name
    entity.department_id = request.departmentId
    entity.description = _trim_to_none(request.description)
    entity.level = _trim_to_none(request.level)
    entity.salary = max(request.salary or 0, 0)
    entity.status = _normalize_status(request.status)
    db.commit()
    return _to_designation_response(entity)


def delete_designation(db: Session, id_: int) -> None:
    entity = db.get(Designation, id_)
    if entity is None:
        raise EntityNotFoundException(f"Designation {id_} not found")
    db.delete(entity)
    db.commit()
