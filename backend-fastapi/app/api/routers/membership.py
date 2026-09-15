from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.models.users import Users
from app.schemas.common import success
from app.schemas.membership import (
    AssignMembershipRequest,
    CreateOrderRequest,
    PlanChangeRequest,
    UpgradeMembershipRequest,
    VerifyPaymentRequest,
)
from app.services import membership_service as svc

router = APIRouter(prefix="/api/membership", tags=["membership"])


@router.get("/me")
def get_my_membership(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.build_response(db, user)
    return success(result.model_dump(), "Membership retrieved successfully")


@router.get("/member/{memberId}")
def get_member_membership(memberId: int, user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.get_member_membership(db, memberId, user)
    return success(result.model_dump(), "Membership retrieved successfully")


@router.post("/assign/{memberId}")
def assign_membership(
    memberId: int, request: AssignMembershipRequest, user: Users = Depends(get_current_user), db: Session = Depends(get_db)
):
    result = svc.assign_membership(db, memberId, request, user)
    return success(result.model_dump(), "Membership assigned successfully")


@router.post("/request-change")
def request_plan_change(
    request: PlanChangeRequest, user: Users = Depends(get_current_user), db: Session = Depends(get_db)
):
    svc.request_plan_change(db, request, user)
    return success(None, "Plan change requested successfully")


@router.get("/requests")
def get_pending_requests(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.get_pending_requests(db, user)
    return success([r.model_dump() for r in result], "Membership requests retrieved successfully")


@router.post("/requests/{requestId}/dismiss")
def dismiss_request(requestId: int, user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    svc.dismiss_request(db, requestId, user)
    return success(None, "Membership request dismissed")


@router.post("/upgrade")
def upgrade(
    request: UpgradeMembershipRequest = UpgradeMembershipRequest(),
    user: Users = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    result = svc.upgrade(db, request, user)
    return success(result.model_dump(), "Membership upgraded successfully")


@router.post("/create-order")
def create_order(request: CreateOrderRequest, user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.create_order(db, request, user)
    return success(result.model_dump(), "Order created successfully")


@router.post("/verify-payment")
def verify_payment(request: VerifyPaymentRequest, user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.verify_and_activate(db, request, user)
    return success(result.model_dump(), "Payment verified and membership activated")


@router.post("/cancel")
def cancel(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.cancel(db, user)
    return success(result.model_dump(), "Membership cancelled successfully")
