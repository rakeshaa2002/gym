from datetime import date

from fastapi import APIRouter, Depends, Header
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.schemas.attendance import DeviceCheckInRequest, EnrollFingerprintRequest, KioskCheckInRequest
from app.schemas.common import success
from app.models.users import Users
from app.services import attendance_service as svc

router = APIRouter(prefix="/api/attendance", tags=["attendance"])


@router.post("/checkin")
def device_checkin(
    request: DeviceCheckInRequest,
    x_device_key: str | None = Header(default=None, alias="X-Device-Key"),
    db: Session = Depends(get_db),
):
    result = svc.device_check_in(db, request.fingerprintId, request.userId, request.email, request.deviceId, x_device_key)
    return success(result.model_dump(), result.message)


@router.post("/kiosk")
def kiosk_checkin(request: KioskCheckInRequest, user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.kiosk_check_in(db, request.identifier, user)
    return success(result.model_dump(), result.message)


@router.post("/self")
def self_checkin(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.self_check_in(db, user)
    return success(result.model_dump(), result.message)


@router.post("/manual/{memberId}")
def manual_checkin(memberId: int, user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.manual_check_in(db, memberId, user)
    return success(result.model_dump(), result.message)


@router.put("/enroll/{memberId}")
def enroll_fingerprint(
    memberId: int, request: EnrollFingerprintRequest, user: Users = Depends(get_current_user), db: Session = Depends(get_db)
):
    svc.enroll_fingerprint(db, memberId, request.fingerprintId, user)
    return success(None, "Fingerprint enrolled successfully")


@router.get("/me")
def my_attendance(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.get_my_attendance(db, user)
    return success([r.model_dump() for r in result], "Attendance retrieved successfully")


@router.get("/members")
def enrollable_members(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.get_enrollable_members(db, user)
    return success([r.model_dump() for r in result], "Members retrieved successfully")


@router.get("")
def list_attendance(date_: date | None = None, user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.get_all(db, user, date_)
    return success([r.model_dump() for r in result], "Attendance retrieved successfully")


@router.get("/today")
def today_attendance(user: Users = Depends(get_current_user), db: Session = Depends(get_db)):
    result = svc.get_today(db, user)
    return success([r.model_dump() for r in result], "Attendance retrieved successfully")
