from datetime import datetime

from pydantic import BaseModel


class HeadOfficeRequest(BaseModel):
    name: str
    location: str | None = None
    address: str | None = None
    phone: str | None = None
    email: str | None = None
    status: str | None = "ACTIVE"


class HeadOfficeResponse(BaseModel):
    id: int
    name: str
    location: str | None
    address: str | None
    phone: str | None
    email: str | None
    status: str
    createdAt: datetime
    updatedAt: datetime


class BranchRequest(BaseModel):
    name: str
    headOfficeId: int
    location: str | None = None
    address: str | None = None
    phone: str | None = None
    email: str | None = None
    managerName: str | None = None
    status: str | None = "ACTIVE"


class BranchResponse(BaseModel):
    id: int
    name: str
    headOfficeId: int
    location: str | None
    address: str | None
    phone: str | None
    email: str | None
    managerName: str | None
    status: str
    createdAt: datetime
    updatedAt: datetime


class DepartmentRequest(BaseModel):
    name: str
    branchId: int
    description: str | None = None
    status: str | None = "ACTIVE"


class DepartmentResponse(BaseModel):
    id: int
    name: str
    branchId: int
    description: str | None
    status: str
    createdAt: datetime
    updatedAt: datetime


class DesignationRequest(BaseModel):
    name: str
    departmentId: int
    description: str | None = None
    level: str | None = None
    salary: int | None = 0
    status: str | None = "ACTIVE"


class DesignationResponse(BaseModel):
    id: int
    name: str
    departmentId: int
    description: str | None
    level: str | None
    salary: int | None
    status: str
    createdAt: datetime
    updatedAt: datetime


class TeamRequest(BaseModel):
    name: str
    departmentId: int
    description: str | None = None
    teamLead: str | None = None
    memberCount: int | None = 0
    status: str | None = "ACTIVE"


class TeamResponse(BaseModel):
    id: int
    name: str
    departmentId: int
    description: str | None
    teamLead: str | None
    memberCount: int
    status: str
    createdAt: datetime
    updatedAt: datetime
