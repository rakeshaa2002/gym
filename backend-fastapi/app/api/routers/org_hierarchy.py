from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db, require_role
from app.schemas.common import success
from app.schemas.org import (
    BranchRequest,
    DepartmentRequest,
    DesignationRequest,
    HeadOfficeRequest,
    TeamRequest,
)
from app.services import org_hierarchy_service as svc

router = APIRouter(prefix="/api/org", tags=["organization"])

staff_view = require_role("SUPER_ADMIN", "ADMIN", "MANAGER")
staff_write = require_role("SUPER_ADMIN", "ADMIN")
super_admin_only = require_role("SUPER_ADMIN")


# ---------- Head Offices ----------


@router.get("/head-offices")
def list_head_offices(_=Depends(super_admin_only), db: Session = Depends(get_db)):
    return success([o.model_dump() for o in svc.get_all_head_offices(db)], "Head offices retrieved successfully")


@router.post("/head-offices", status_code=201)
def create_head_office(request: HeadOfficeRequest, _=Depends(super_admin_only), db: Session = Depends(get_db)):
    result = svc.create_head_office(db, request)
    return success(result.model_dump(), "Head office created successfully", status=201)


@router.put("/head-offices/{id}")
def update_head_office(id: int, request: HeadOfficeRequest, _=Depends(super_admin_only), db: Session = Depends(get_db)):
    result = svc.update_head_office(db, id, request)
    return success(result.model_dump(), "Head office updated successfully")


@router.delete("/head-offices/{id}")
def delete_head_office(id: int, _=Depends(super_admin_only), db: Session = Depends(get_db)):
    svc.delete_head_office(db, id)
    return success(None, "Head office deleted successfully")


# ---------- Branches ----------


@router.get("/branches")
def list_branches(headOfficeId: int | None = None, _=Depends(staff_view), db: Session = Depends(get_db)):
    data = svc.get_branches_by_head_office(db, headOfficeId) if headOfficeId is not None else svc.get_all_branches(db)
    return success([o.model_dump() for o in data], "Branches retrieved successfully")


@router.post("/branches", status_code=201)
def create_branch(request: BranchRequest, _=Depends(staff_write), db: Session = Depends(get_db)):
    result = svc.create_branch(db, request)
    return success(result.model_dump(), "Branch created successfully", status=201)


@router.put("/branches/{id}")
def update_branch(id: int, request: BranchRequest, _=Depends(staff_write), db: Session = Depends(get_db)):
    result = svc.update_branch(db, id, request)
    return success(result.model_dump(), "Branch updated successfully")


@router.delete("/branches/{id}")
def delete_branch(id: int, _=Depends(staff_write), db: Session = Depends(get_db)):
    svc.delete_branch(db, id)
    return success(None, "Branch deleted successfully")


# ---------- Departments ----------


@router.get("/departments")
def list_departments(branchId: int | None = None, _=Depends(staff_view), db: Session = Depends(get_db)):
    data = svc.get_departments_by_branch(db, branchId) if branchId is not None else svc.get_all_departments(db)
    return success([o.model_dump() for o in data], "Departments retrieved successfully")


@router.post("/departments", status_code=201)
def create_department(request: DepartmentRequest, _=Depends(staff_write), db: Session = Depends(get_db)):
    result = svc.create_department(db, request)
    return success(result.model_dump(), "Department created successfully", status=201)


@router.put("/departments/{id}")
def update_department(id: int, request: DepartmentRequest, _=Depends(staff_write), db: Session = Depends(get_db)):
    result = svc.update_department(db, id, request)
    return success(result.model_dump(), "Department updated successfully")


@router.delete("/departments/{id}")
def delete_department(id: int, _=Depends(staff_write), db: Session = Depends(get_db)):
    svc.delete_department(db, id)
    return success(None, "Department deleted successfully")


# ---------- Teams ----------


@router.get("/teams")
def list_teams(departmentId: int | None = None, _=Depends(staff_view), db: Session = Depends(get_db)):
    data = svc.get_teams_by_department(db, departmentId) if departmentId is not None else svc.get_all_teams(db)
    return success([o.model_dump() for o in data], "Teams retrieved successfully")


@router.post("/teams", status_code=201)
def create_team(request: TeamRequest, _=Depends(staff_write), db: Session = Depends(get_db)):
    result = svc.create_team(db, request)
    return success(result.model_dump(), "Team created successfully", status=201)


@router.put("/teams/{id}")
def update_team(id: int, request: TeamRequest, _=Depends(staff_write), db: Session = Depends(get_db)):
    result = svc.update_team(db, id, request)
    return success(result.model_dump(), "Team updated successfully")


@router.delete("/teams/{id}")
def delete_team(id: int, _=Depends(staff_write), db: Session = Depends(get_db)):
    svc.delete_team(db, id)
    return success(None, "Team deleted successfully")


# ---------- Designations ----------


@router.get("/designations")
def list_designations(departmentId: int | None = None, _=Depends(staff_view), db: Session = Depends(get_db)):
    data = svc.get_designations_by_department(db, departmentId) if departmentId is not None else svc.get_all_designations(db)
    return success([o.model_dump() for o in data], "Designations retrieved successfully")


@router.post("/designations", status_code=201)
def create_designation(request: DesignationRequest, _=Depends(staff_write), db: Session = Depends(get_db)):
    result = svc.create_designation(db, request)
    return success(result.model_dump(), "Designation created successfully", status=201)


@router.put("/designations/{id}")
def update_designation(id: int, request: DesignationRequest, _=Depends(staff_write), db: Session = Depends(get_db)):
    result = svc.update_designation(db, id, request)
    return success(result.model_dump(), "Designation updated successfully")


@router.delete("/designations/{id}")
def delete_designation(id: int, _=Depends(staff_write), db: Session = Depends(get_db)):
    svc.delete_designation(db, id)
    return success(None, "Designation deleted successfully")


# ---------- Cascade dropdowns (any authenticated user) ----------


@router.get("/cascade/branches-for-employee")
def cascade_branches(_=Depends(get_current_user), db: Session = Depends(get_db)):
    return success([o.model_dump() for o in svc.get_all_branches(db)], "Branches retrieved successfully")


@router.get("/cascade/departments-for-employee")
def cascade_departments(branchId: int, _=Depends(get_current_user), db: Session = Depends(get_db)):
    return success([o.model_dump() for o in svc.get_departments_by_branch(db, branchId)], "Departments retrieved successfully")


@router.get("/cascade/teams-for-employee")
def cascade_teams(departmentId: int, _=Depends(get_current_user), db: Session = Depends(get_db)):
    return success([o.model_dump() for o in svc.get_teams_by_department(db, departmentId)], "Teams retrieved successfully")


@router.get("/cascade/designations-for-employee")
def cascade_designations(departmentId: int, _=Depends(get_current_user), db: Session = Depends(get_db)):
    return success([o.model_dump() for o in svc.get_designations_by_department(db, departmentId)], "Designations retrieved successfully")
