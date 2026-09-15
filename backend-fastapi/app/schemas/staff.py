from datetime import datetime

from pydantic import BaseModel

from app.models.role import Role


class _HrProfileFields(BaseModel):
    """Shared personal/employment/bank/document/family/reference fields — identical
    field set on CreateAdminRequest/CreateTrainerRequest and Admin/Trainer/Manager
    response DTOs in the Java source. Dates are plain strings, matching Java's
    lenient server-side date parsing (invalid/blank values are silently ignored)."""

    dateOfBirth: str | None = None
    gender: str | None = None
    bloodGroup: str | None = None
    personalEmail: str | None = None
    alternatePhone: str | None = None
    emergencyContact: str | None = None
    emergencyPhone: str | None = None
    currentAddress: str | None = None
    permanentAddress: str | None = None
    city: str | None = None
    state: str | None = None
    pincode: str | None = None
    employmentType: str | None = None
    workLocation: str | None = None
    reportingManagerName: str | None = None
    probationEndDate: str | None = None
    panNumber: str | None = None
    aadharNumber: str | None = None
    bankName: str | None = None
    bankAccountNumber: str | None = None
    bankIfscCode: str | None = None
    bankAccountType: str | None = None
    bankAccountHolderName: str | None = None
    bankBranch: str | None = None
    bankDocumentPath: str | None = None
    qualificationDocumentPath: str | None = None
    certificationDocumentPath: str | None = None
    idProofDocumentPath: str | None = None
    addressProofDocumentPath: str | None = None
    resumeDocumentPath: str | None = None
    offerLetterDocumentPath: str | None = None
    candidatePhotoPath: str | None = None
    aadharCardDocumentPath: str | None = None
    panCardDocumentPath: str | None = None
    experienceCertificateDocumentPath: str | None = None
    courseCertificatePath: str | None = None
    educationCertificatePath: str | None = None
    fatherName: str | None = None
    motherName: str | None = None
    maritalStatus: str | None = None
    spouseName: str | None = None
    location: str | None = None
    emergencyContactRelationship: str | None = None
    emergencyContactName2: str | None = None
    emergencyContactRelationship2: str | None = None
    emergencyPhone2: str | None = None
    referenceName1: str | None = None
    referencePhone1: str | None = None
    referenceName2: str | None = None
    referencePhone2: str | None = None
    previousEmployment1: str | None = None
    previousEmployment2: str | None = None
    joiningBranchName: str | None = None
    sourcePlatform: str | None = None
    pfUan: str | None = None
    esiNumber: str | None = None
    declarationDate: str | None = None
    declarationPlace: str | None = None


class _OrgFields(BaseModel):
    headOfficeId: int | None = None
    branchId: int | None = None
    departmentId: int | None = None
    teamId: int | None = None
    designationId: int | None = None
    reportsToId: int | None = None


class CreateAdminRequest(_HrProfileFields, _OrgFields):
    email: str
    password: str
    keepPassword: bool | None = False
    firstName: str
    lastName: str | None = None
    department: str
    phone: str
    employeeId: str
    qualification: str
    joinDate: str | None = None
    bio: str | None = None


class CreateTrainerRequest(_HrProfileFields, _OrgFields):
    email: str
    password: str
    keepPassword: bool | None = False
    firstName: str
    lastName: str | None = None
    specialization: str
    experienceYears: int
    certification: str
    phone: str
    qualification: str
    ratePerHour: float
    joinDate: str | None = None
    bio: str | None = None
    languages: str | None = None
    rating: float | None = None
    totalClientsTrained: int | None = None


class CreateSuperAdminRequest(BaseModel):
    email: str
    password: str
    keepPassword: bool | None = False
    firstName: str
    lastName: str | None = None


class CreateCorporateHrRequest(BaseModel):
    companyName: str
    email: str
    password: str
    keepPassword: bool | None = False


class CreateCounselorRequest(BaseModel):
    email: str
    password: str
    keepPassword: bool | None = False
    firstName: str
    lastName: str | None = None
    phone: str


class AdminResponse(_HrProfileFields):
    id: int
    email: str
    firstName: str
    lastName: str | None
    department: str
    phone: str
    employeeId: str
    qualification: str
    isActive: bool
    createdByName: str | None = None
    headOfficeId: int | None = None
    branchId: int | None = None
    departmentId: int | None = None
    teamId: int | None = None
    designationId: int | None = None
    joinDate: datetime | None
    bio: str | None
    reportsToId: int | None = None
    reportsToName: str | None = None
    reportsToRole: str | None = None
    createdAt: datetime
    updatedAt: datetime


class TrainerResponse(_HrProfileFields):
    id: int
    email: str
    firstName: str
    lastName: str | None
    specialization: str
    experienceYears: int
    certification: str
    phone: str
    qualification: str
    ratePerHour: float
    isActive: bool
    createdByName: str | None = None
    headOfficeId: int | None = None
    branchId: int | None = None
    departmentId: int | None = None
    teamId: int | None = None
    designationId: int | None = None
    joinDate: datetime | None
    bio: str | None
    languages: str | None
    rating: float
    totalClientsTrained: int
    reportsToId: int | None = None
    reportsToName: str | None = None
    reportsToRole: str | None = None
    createdAt: datetime
    updatedAt: datetime


class CounselorResponse(BaseModel):
    id: int
    email: str
    firstName: str
    lastName: str | None
    phone: str
    isActive: bool
    joinDate: datetime | None
    createdAt: datetime
    updatedAt: datetime


class SuperAdminResponse(BaseModel):
    id: int
    email: str
    firstName: str
    lastName: str | None
    isActive: bool

    @property
    def fullName(self) -> str:
        return f"{self.firstName} {self.lastName}" if self.lastName else self.firstName

    @property
    def status(self) -> str:
        return "ACTIVE" if self.isActive else "INACTIVE"


class CorporateHrResponse(BaseModel):
    id: int
    companyName: str | None = None
    email: str
    role: Role
    active: bool
    createdAt: datetime
    updatedAt: datetime


# ---------- Customers (FitnessUser) ----------


class CreateCustomerByTrainerRequest(BaseModel):
    email: str
    password: str
    firstName: str
    lastName: str | None = None
    weight: float
    height: float
    bloodGroup: str
    age: int
    gender: str
    phone: str
    address: str
    city: str
    medicalConditions: str | None = None
    emergencyContact: str
    emergencyPhone: str
    teamId: int | None = None
    assignedTrainerId: int | None = None
    photoPath: str | None = None
    idProofPath: str | None = None
    bodyFat: float | None = None
    isFrozen: bool | None = None
    referredBy: str | None = None


class CreateCustomerSelfRequest(BaseModel):
    email: str
    password: str
    firstName: str
    lastName: str | None = None


class UpdateCustomerDetailsRequest(BaseModel):
    email: str | None = None
    firstName: str | None = None
    lastName: str | None = None
    password: str | None = None
    weight: float
    height: float
    bloodGroup: str
    age: int
    gender: str
    phone: str
    address: str
    city: str
    medicalConditions: str | None = None
    emergencyContact: str
    emergencyPhone: str
    headOfficeId: int | None = None
    branchId: int | None = None
    departmentId: int | None = None
    teamId: int | None = None
    designationId: int | None = None
    assignedTrainerId: int | None = None
    photoPath: str | None = None
    idProofPath: str | None = None
    bodyFat: float | None = None
    isFrozen: bool | None = None
    referredBy: str | None = None


class CustomerResponse(BaseModel):
    id: int
    email: str
    firstName: str | None
    lastName: str | None
    headOfficeId: int | None
    branchId: int | None
    departmentId: int | None
    teamId: int | None
    designationId: int | None
    weight: float | None
    height: float | None
    bloodGroup: str | None
    age: int | None
    gender: str | None
    phone: str | None
    address: str | None
    city: str | None
    medicalConditions: str | None
    emergencyContact: str | None
    emergencyPhone: str | None
    isActive: bool
    isApproved: bool
    assignedTrainerId: int | None
    assignedTrainerName: str | None
    trainerId: int | None
    createdById: int | None
    photoPath: str | None
    idProofPath: str | None
    assignedDietPlanId: int | None
    assignedDietPlanName: str | None
    assignedWorkoutPlanId: int | None
    assignedWorkoutPlanName: str | None
    bodyFat: float | None
    isFrozen: bool
    referredBy: str | None
    membershipExpiry: str | None
    membershipPlan: str
    membershipPlanId: int | None
    accessStartTime: str | None
    accessEndTime: str | None


class MyProfileRequest(BaseModel):
    name: str | None = None
    weight: float | None = None
    height: float | None = None
    age: int | None = None
    gender: str | None = None
    dateOfBirth: str | None = None
    bloodGroup: str | None = None
    phone: str | None = None
    address: str | None = None
    city: str | None = None
    bio: str | None = None
    fitnessGoals: str | None = None


class MyProfileResponse(BaseModel):
    id: int
    name: str
    email: str
    role: str
    weight: float | None
    height: float | None
    age: int | None
    gender: str | None
    dateOfBirth: str | None
    bloodGroup: str | None
    phone: str | None
    address: str | None
    city: str | None
    bio: str | None
    fitnessGoals: str | None
    hasFitnessProfile: bool


class AtRiskMemberResponse(BaseModel):
    id: int
    firstName: str | None
    lastName: str | None
    email: str
    phone: str | None
    reasons: list[str]


class ReportingOptionResponse(BaseModel):
    id: int
    name: str
    email: str
    role: Role
    branchId: int | None
