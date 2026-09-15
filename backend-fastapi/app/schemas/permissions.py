from pydantic import BaseModel

from app.models.role import Role


class PagePermissionDto(BaseModel):
    pageKey: str
    canView: bool
    canCreate: bool
    canEdit: bool
    canDelete: bool


class CurrentUserPermissionsResponse(BaseModel):
    role: Role
    permissions: list[PagePermissionDto]


class PermissionPageDto(BaseModel):
    pageKey: str
    label: str
    routePath: str
    category: str
    sortOrder: int


class RolePermissionsDto(BaseModel):
    role: Role
    permissions: list[PagePermissionDto]


class PermissionsMatrixResponse(BaseModel):
    pages: list[PermissionPageDto]
    roles: list[RolePermissionsDto]


class PermissionMatrixUpdateRequest(BaseModel):
    roles: list[RolePermissionsDto] | None = None
