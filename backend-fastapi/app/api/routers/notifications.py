from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.models.users import Users
from app.schemas.common import success
from app.services import notification_service as svc

router = APIRouter(prefix="/api/notifications", tags=["notifications"])


@router.get("")
def get_my_notifications(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.get_my_notifications(db, user)
    return success([r.model_dump() for r in result], "Notifications retrieved successfully")
