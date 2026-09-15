from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db, require_super_admin
from app.models.users import Users
from app.schemas.common import success
from app.schemas.permissions import PermissionMatrixUpdateRequest
from app.services import permission_service

router = APIRouter(prefix="/api/access", tags=["permissions"])


@router.get("/permissions/me")
def get_current_user_permissions(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    response = permission_service.get_current_user_permissions(db, user.email)
    return success(response.model_dump(), "Permissions retrieved successfully")


@router.get("/permissions/matrix")
def get_matrix(_: Users = Depends(require_super_admin), db: Session = Depends(get_db)):
    response = permission_service.get_matrix(db)
    return success(response.model_dump(), "Permission matrix retrieved successfully")


@router.put("/permissions/matrix")
def save_matrix(
    request: PermissionMatrixUpdateRequest,
    _: Users = Depends(require_super_admin),
    db: Session = Depends(get_db),
):
    permission_service.save_matrix(db, request)
    return success(None, "Permission matrix saved successfully")
