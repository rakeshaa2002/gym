from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_db
from app.schemas.common import success
from app.schemas.membership import MembershipPlanRequest
from app.services import membership_plan_service as svc

router = APIRouter(prefix="/api/membership-plans", tags=["membership-plans"])


@router.get("")
def list_plans(activeOnly: bool = False, db: Session = Depends(get_db)):
    result = svc.get_all(db, activeOnly)
    return success([r.model_dump() for r in result], "Membership plans retrieved successfully")


@router.get("/{id}")
def get_plan(id: int, db: Session = Depends(get_db)):
    result = svc.get_by_id(db, id)
    return success(result.model_dump(), "Membership plan retrieved successfully")


@router.post("", status_code=201)
def create_plan(request: MembershipPlanRequest, db: Session = Depends(get_db)):
    result = svc.create(db, request)
    return success(result.model_dump(), "Membership plan created successfully", status=201)


@router.put("/{id}")
def update_plan(id: int, request: MembershipPlanRequest, db: Session = Depends(get_db)):
    result = svc.update(db, id, request)
    return success(result.model_dump(), "Membership plan updated successfully")


@router.delete("/{id}")
def delete_plan(id: int, db: Session = Depends(get_db)):
    svc.delete(db, id)
    return success(None, "Membership plan deleted successfully")
