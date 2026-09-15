from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.models.role import Role
from app.models.users import Users
from app.schemas.common import success
from app.schemas.staff import (
    CreateAdminRequest,
    CreateCorporateHrRequest,
    CreateCounselorRequest,
    CreateCustomerByTrainerRequest,
    CreateCustomerSelfRequest,
    CreateSuperAdminRequest,
    CreateTrainerRequest,
    MyProfileRequest,
    UpdateCustomerDetailsRequest,
)
from app.services import user_management_service as svc

router = APIRouter(prefix="/api/users", tags=["user-management"])


# ---------- Super Admin ----------


@router.post("/super-admin", status_code=201)
def create_super_admin(request: CreateSuperAdminRequest, creatorId: int, db: Session = Depends(get_db)):
    result = svc.create_super_admin(db, request, creatorId)
    return success(result.model_dump(), "Super admin created successfully", status=201)


@router.get("/super-admin/{superAdminId}")
def get_super_admin(superAdminId: int, requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_super_admin(db, superAdminId, requesterId)
    return success(result.model_dump(), "Super admin retrieved successfully")


@router.put("/super-admin/{superAdminId}")
def update_super_admin(superAdminId: int, request: CreateSuperAdminRequest, updaterId: int, db: Session = Depends(get_db)):
    result = svc.update_super_admin(db, superAdminId, request, updaterId)
    return success(result.model_dump(), "Super admin updated successfully")


@router.delete("/super-admin/{superAdminId}")
def delete_super_admin(superAdminId: int, deleterId: int, db: Session = Depends(get_db)):
    svc.delete_super_admin(db, superAdminId, deleterId)
    return success(None, "Super admin deleted successfully")


@router.get("/super-admins")
def list_super_admins(requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_all_super_admins(db, requesterId)
    return success([r.model_dump() for r in result], "Super admins retrieved successfully")


# ---------- Admin ----------


@router.post("/admin", status_code=201)
def create_admin(request: CreateAdminRequest, creatorId: int, db: Session = Depends(get_db)):
    result = svc.create_admin(db, request, creatorId)
    return success(result.model_dump(), "Admin created successfully", status=201)


@router.get("/admin/{adminId}")
def get_admin(adminId: int, requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_admin(db, adminId, requesterId)
    return success(result.model_dump(), "Admin retrieved successfully")


@router.put("/admin/{adminId}")
def update_admin(adminId: int, request: CreateAdminRequest, updaterId: int, db: Session = Depends(get_db)):
    result = svc.update_admin(db, adminId, request, updaterId)
    return success(result.model_dump(), "Admin updated successfully")


@router.delete("/admin/{adminId}")
def delete_admin(adminId: int, deleterId: int, db: Session = Depends(get_db)):
    svc.delete_admin(db, adminId, deleterId)
    return success(None, "Admin deleted successfully")


@router.get("/admins")
def list_admins(requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_all_admins(db, requesterId)
    return success([r.model_dump() for r in result], "Admins retrieved successfully")


# ---------- Corporate HR ----------


@router.post("/corporate-hr", status_code=201)
def create_corporate_hr(request: CreateCorporateHrRequest, creatorId: int, db: Session = Depends(get_db)):
    result = svc.create_corporate_hr(db, request, creatorId)
    return success(result.model_dump(), "Corporate HR created successfully", status=201)


@router.put("/corporate-hr/{id}")
def update_corporate_hr(id: int, request: CreateCorporateHrRequest, updaterId: int, db: Session = Depends(get_db)):
    result = svc.update_corporate_hr(db, id, request, updaterId)
    return success(result.model_dump(), "Corporate HR updated successfully")


@router.delete("/corporate-hr/{id}")
def delete_corporate_hr(id: int, deleterId: int, db: Session = Depends(get_db)):
    svc.delete_corporate_hr(db, id, deleterId)
    return success(None, "Corporate HR deleted successfully")


@router.get("/corporate-hr")
def list_corporate_hr(requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_all_corporate_hr(db, requesterId)
    return success([r.model_dump() for r in result], "Corporate HR accounts retrieved successfully")


# ---------- Manager ----------


@router.post("/manager", status_code=201)
def create_manager(request: CreateAdminRequest, creatorId: int, db: Session = Depends(get_db)):
    result = svc.create_manager(db, request, creatorId)
    return success(result.model_dump(), "Manager created successfully", status=201)


@router.get("/manager/{managerId}")
def get_manager(managerId: int, requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_manager(db, managerId, requesterId)
    return success(result.model_dump(), "Manager retrieved successfully")


@router.put("/manager/{managerId}")
def update_manager(managerId: int, request: CreateAdminRequest, updaterId: int, db: Session = Depends(get_db)):
    result = svc.update_manager(db, managerId, request, updaterId)
    return success(result.model_dump(), "Manager updated successfully")


@router.delete("/manager/{managerId}")
def delete_manager(managerId: int, deleterId: int, db: Session = Depends(get_db)):
    svc.delete_manager(db, managerId, deleterId)
    return success(None, "Manager deleted successfully")


@router.get("/managers")
def list_managers(requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_all_managers(db, requesterId)
    return success([r.model_dump() for r in result], "Managers retrieved successfully")


# ---------- Trainer ----------


@router.post("/trainer", status_code=201)
def create_trainer(request: CreateTrainerRequest, creatorId: int, db: Session = Depends(get_db)):
    result = svc.create_trainer(db, request, creatorId)
    return success(result.model_dump(), "Trainer created successfully", status=201)


@router.get("/trainer/{trainerId}")
def get_trainer(trainerId: int, requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_trainer(db, trainerId, requesterId)
    return success(result.model_dump(), "Trainer retrieved successfully")


@router.put("/trainer/{trainerId}")
def update_trainer(trainerId: int, request: CreateTrainerRequest, updaterId: int, db: Session = Depends(get_db)):
    result = svc.update_trainer(db, trainerId, request, updaterId)
    return success(result.model_dump(), "Trainer updated successfully")


@router.delete("/trainer/{trainerId}")
def delete_trainer(trainerId: int, deleterId: int, db: Session = Depends(get_db)):
    svc.delete_trainer(db, trainerId, deleterId)
    return success(None, "Trainer deleted successfully")


@router.get("/trainers")
def list_trainers(requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_all_trainers(db, requesterId)
    return success([r.model_dump() for r in result], "Trainers retrieved successfully")


# ---------- Counselor ----------


@router.post("/counselor", status_code=201)
def create_counselor(request: CreateCounselorRequest, creatorId: int, db: Session = Depends(get_db)):
    result = svc.create_counselor(db, request, creatorId)
    return success(result.model_dump(), "Counselor created successfully", status=201)


@router.get("/counselor/{counselorId}")
def get_counselor(counselorId: int, requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_counselor(db, counselorId, requesterId)
    return success(result.model_dump(), "Counselor retrieved successfully")


@router.put("/counselor/{counselorId}")
def update_counselor(counselorId: int, request: CreateCounselorRequest, updaterId: int, db: Session = Depends(get_db)):
    result = svc.update_counselor(db, counselorId, request, updaterId)
    return success(result.model_dump(), "Counselor updated successfully")


@router.delete("/counselor/{counselorId}")
def delete_counselor(counselorId: int, deleterId: int, db: Session = Depends(get_db)):
    svc.delete_counselor(db, counselorId, deleterId)
    return success(None, "Counselor deleted successfully")


@router.get("/counselors")
def list_counselors(requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_all_counselors(db, requesterId)
    return success([r.model_dump() for r in result], "Counselors retrieved successfully")


# ---------- Customers ----------


@router.post("/customer/self-register", status_code=201)
def customer_self_register(request: CreateCustomerSelfRequest, db: Session = Depends(get_db)):
    result = svc.create_customer_self(db, request)
    return success(result.model_dump(), "Registration successful! Please wait for trainer activation", status=201)


@router.post("/customer/by-trainer", status_code=201)
def customer_by_trainer(request: CreateCustomerByTrainerRequest, trainerId: int, db: Session = Depends(get_db)):
    result = svc.create_customer_by_trainer(db, request, trainerId)
    return success(result.model_dump(), "Customer created successfully", status=201)


@router.get("/customer/{customerId}")
def get_customer(customerId: int, requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_customer(db, customerId, requesterId)
    return success(result.model_dump(), "Customer retrieved successfully")


@router.put("/customer/{customerId}")
def update_customer(customerId: int, request: UpdateCustomerDetailsRequest, updaterId: int, db: Session = Depends(get_db)):
    result = svc.update_customer(db, customerId, request, updaterId)
    return success(result.model_dump(), "Customer updated successfully")


@router.delete("/customer/{customerId}")
def delete_customer(customerId: int, deleterId: int, db: Session = Depends(get_db)):
    svc.delete_customer(db, customerId, deleterId)
    return success(None, "Customer deleted successfully")


@router.get("/customers")
def list_customers(requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_all_customers(db, requesterId)
    return success([r.model_dump() for r in result], "Customers retrieved successfully")


@router.get("/customers/assigned-to/{trainerId}")
def customers_assigned_to_trainer(trainerId: int, db: Session = Depends(get_db)):
    result = svc.get_customers_assigned_to_trainer(db, trainerId)
    return success([r.model_dump() for r in result], "Customers retrieved successfully")


@router.get("/customers/expiring")
def customers_expiring(requesterId: int, days: int = 30, db: Session = Depends(get_db)):
    result = svc.get_expiring_members(db, days, requesterId)
    return success([r.model_dump() for r in result], "Expiring members retrieved successfully")


@router.get("/customers/at-risk")
def customers_at_risk(requesterId: int, db: Session = Depends(get_db)):
    result = svc.get_at_risk_members(db, requesterId)
    return success([r.model_dump() for r in result], "At-risk members retrieved successfully")


@router.post("/customer/{customerId}/activate")
def activate_customer(customerId: int, trainerId: int, db: Session = Depends(get_db)):
    svc.activate_customer(db, customerId, trainerId)
    return success(None, "Customer activated successfully")


@router.put("/{userId}/status")
def set_user_status(userId: int, active: bool, updaterId: int, db: Session = Depends(get_db)):
    svc.set_user_active_status(db, userId, active, updaterId)
    return success(None, "Status updated successfully")


# ---------- Profile ----------


@router.get("/me/profile")
def get_my_profile(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.get_my_profile(db, user)
    return success(result.model_dump(), "Profile retrieved successfully")


@router.put("/me/profile")
def update_my_profile(request: MyProfileRequest, user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.update_my_profile(db, user, request)
    return success(result.model_dump(), "Profile updated successfully")


@router.post("/me/onboarding-complete")
def complete_onboarding(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    svc.complete_onboarding(db, user)
    return success(None, "Onboarding completed")


# ---------- Reporting options ----------


@router.get("/reporting-options")
def reporting_options(role: Role, requesterId: int, branchId: int | None = None, db: Session = Depends(get_db)):
    result = svc.get_reporting_options(db, role, branchId, requesterId)
    return success([r.model_dump() for r in result], "Reporting options retrieved successfully")
