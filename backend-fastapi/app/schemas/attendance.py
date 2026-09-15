from pydantic import BaseModel


class DeviceCheckInRequest(BaseModel):
    fingerprintId: str | None = None
    userId: int | None = None
    email: str | None = None
    deviceId: str | None = None


class EnrollFingerprintRequest(BaseModel):
    fingerprintId: str


class KioskCheckInRequest(BaseModel):
    identifier: str


class AccessDecisionResponse(BaseModel):
    accessGranted: bool
    openGate: bool
    action: str
    memberId: int | None
    memberName: str | None
    time: str
    message: str


class AttendanceResponse(BaseModel):
    id: int
    memberId: int | None
    memberName: str | None
    memberEmail: str | None
    attendanceDate: str
    checkInTime: str
    checkOutTime: str | None
    method: str | None
    deviceId: str | None
    status: str


class AttendanceMemberResponse(BaseModel):
    id: int
    name: str
    email: str
