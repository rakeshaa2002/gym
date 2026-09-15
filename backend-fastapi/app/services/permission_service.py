from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.exceptions import InvalidOperationException
from app.models.permission import PermissionPage, RolePagePermission
from app.models.role import Role
from app.models.users import Users
from app.schemas.permissions import (
    CurrentUserPermissionsResponse,
    PagePermissionDto,
    PermissionMatrixUpdateRequest,
    PermissionPageDto,
    PermissionsMatrixResponse,
    RolePermissionsDto,
)


def _sorted_pages(db: Session) -> list[PermissionPage]:
    pages = list(db.scalars(select(PermissionPage)))
    return sorted(pages, key=lambda p: (p.sort_order, p.label))


def _to_page_dto(page: PermissionPage) -> PermissionPageDto:
    return PermissionPageDto(
        pageKey=page.page_key,
        label=page.label,
        routePath=page.route_path,
        category=page.category,
        sortOrder=page.sort_order,
    )


def get_permissions_for_role(db: Session, role: Role, pages: list[PermissionPage] | None = None) -> list[PagePermissionDto]:
    pages = pages if pages is not None else _sorted_pages(db)
    rows = db.scalars(select(RolePagePermission).where(RolePagePermission.role == role))
    by_page = {row.page_key: row for row in rows}

    result = []
    for page in pages:
        rp = by_page.get(page.page_key)
        if rp is not None:
            result.append(
                PagePermissionDto(
                    pageKey=page.page_key,
                    canView=rp.can_view,
                    canCreate=rp.can_create,
                    canEdit=rp.can_edit,
                    canDelete=rp.can_delete,
                )
            )
        else:
            result.append(
                PagePermissionDto(pageKey=page.page_key, canView=False, canCreate=False, canEdit=False, canDelete=False)
            )
    return result


def get_current_user_permissions(db: Session, email: str) -> CurrentUserPermissionsResponse:
    user = db.scalar(select(Users).where(Users.email == email))
    if user is None:
        raise InvalidOperationException("Current user not found")
    role = Role(user.role)
    return CurrentUserPermissionsResponse(role=role, permissions=get_permissions_for_role(db, role))


def get_matrix(db: Session) -> PermissionsMatrixResponse:
    page_entities = _sorted_pages(db)
    pages = [_to_page_dto(p) for p in page_entities]
    roles = [
        RolePermissionsDto(role=role, permissions=get_permissions_for_role(db, role, page_entities))
        for role in Role
    ]
    return PermissionsMatrixResponse(pages=pages, roles=roles)


def save_matrix(db: Session, request: PermissionMatrixUpdateRequest) -> None:
    if request is None or request.roles is None:
        raise ValueError("Permission matrix is required")

    page_keys = {p.page_key for p in db.scalars(select(PermissionPage))}

    for role_dto in request.roles:
        if role_dto is None or role_dto.role is None:
            continue

        submitted = {
            perm.pageKey: perm
            for perm in (role_dto.permissions or [])
            if perm is not None and perm.pageKey in page_keys
        }

        if not submitted:
            db.query(RolePagePermission).filter(RolePagePermission.role == role_dto.role).delete()
            continue

        db.query(RolePagePermission).filter(
            RolePagePermission.role == role_dto.role,
            RolePagePermission.page_key.notin_(submitted.keys()),
        ).delete(synchronize_session=False)

        existing = {
            row.page_key: row
            for row in db.scalars(select(RolePagePermission).where(RolePagePermission.role == role_dto.role))
        }

        for page_key, perm in submitted.items():
            entity = existing.get(page_key) or RolePagePermission(role=role_dto.role, page_key=page_key)
            entity.can_view = perm.canView
            entity.can_create = perm.canCreate
            entity.can_edit = perm.canEdit
            entity.can_delete = perm.canDelete
            db.add(entity)

    db.commit()


def has_permission(db: Session, role: Role, page_key: str, action: str) -> bool:
    permission = db.scalar(
        select(RolePagePermission).where(RolePagePermission.role == role, RolePagePermission.page_key == page_key)
    )
    if permission is None:
        return False
    action = (action or "").lower()
    if action == "create":
        return permission.can_create
    if action == "edit":
        return permission.can_edit
    if action == "delete":
        return permission.can_delete
    return permission.can_view
