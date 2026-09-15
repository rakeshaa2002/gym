from pydantic import BaseModel


class MembershipPlanRequest(BaseModel):
    code: str | None = None
    name: str | None = None
    description: str | None = None
    price: int | None = None
    durationDays: int | None = None
    maxSessionMinutes: int | None = None
    unlimitedAccess: bool | None = None
    trainerChat: bool | None = None
    features: str | None = None
    active: bool | None = None


class MembershipPlanResponse(BaseModel):
    id: int
    code: str
    name: str
    description: str | None
    price: int
    durationDays: int
    maxSessionMinutes: int | None
    unlimitedAccess: bool
    trainerChat: bool
    features: list[str]
    active: bool
    updatedAt: str


class AssignMembershipRequest(BaseModel):
    planId: int
    accessStartTime: str | None = None
    accessEndTime: str | None = None
    months: int | None = None


class UpgradeMembershipRequest(BaseModel):
    plan: str | None = "PREMIUM"
    months: int | None = 1


class PlanChangeRequest(BaseModel):
    planId: int
    note: str | None = None


class CreateOrderRequest(BaseModel):
    planId: int
    months: int | None = 1


class OrderResponse(BaseModel):
    orderId: str
    keyId: str
    amount: int
    currency: str
    months: int
    name: str
    description: str
    originalAmount: int
    creditApplied: int


class VerifyPaymentRequest(BaseModel):
    razorpayOrderId: str
    razorpayPaymentId: str
    razorpaySignature: str
    planId: int | None = None
    months: int | None = 1


class MembershipResponse(BaseModel):
    plan: str
    expiry: str | None
    daysLeft: int | None
    premium: bool
    applicable: bool
    planId: int | None
    planName: str | None
    unlimitedAccess: bool
    accessStartTime: str | None
    accessEndTime: str | None
    maxSessionMinutes: int


class MembershipRequestResponse(BaseModel):
    id: int
    memberId: int
    memberName: str
    memberEmail: str
    currentPlan: str
    requestedPlanId: int | None
    requestedPlan: str | None
    note: str | None
    createdAt: str
