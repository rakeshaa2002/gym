import logging
from datetime import datetime

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles

from app.core.config import settings
from app.core.exceptions import (
    DuplicateResourceException,
    EntityNotFoundException,
    ForbiddenException,
    InvalidOperationException,
    UnauthorizedException,
)

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("fitnexus")

app = FastAPI(title="Fitnexus Backend (FastAPI)")

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"],
    allow_headers=["*"],
)


def _error_body(status: int, message: str, request: Request, extra: dict | None = None) -> dict:
    body = {
        "status": status,
        "message": message,
        "timestamp": datetime.now().isoformat(),
        "path": request.url.path,
    }
    if extra:
        body.update(extra)
    return body


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    field_errors: dict[str, str] = {}
    for err in exc.errors():
        loc = err.get("loc", ())
        field = loc[-1] if loc else "body"
        field_errors[str(field)] = err.get("msg", "Invalid value")
    log.warning("Validation error: %s", field_errors)
    return JSONResponse(
        status_code=400,
        content=_error_body(400, "Validation failed", request, {"fieldErrors": field_errors}),
    )


@app.exception_handler(EntityNotFoundException)
async def entity_not_found_handler(request: Request, exc: EntityNotFoundException):
    log.warning("Entity not found: %s", exc)
    return JSONResponse(status_code=404, content=_error_body(404, str(exc), request))


@app.exception_handler(DuplicateResourceException)
async def duplicate_resource_handler(request: Request, exc: DuplicateResourceException):
    log.warning("Duplicate resource: %s", exc)
    return JSONResponse(status_code=409, content=_error_body(409, str(exc), request))


@app.exception_handler(UnauthorizedException)
async def unauthorized_handler(request: Request, exc: UnauthorizedException):
    log.warning("Unauthorized access: %s", exc)
    return JSONResponse(status_code=401, content=_error_body(401, str(exc), request))


@app.exception_handler(ForbiddenException)
async def forbidden_handler(request: Request, exc: ForbiddenException):
    log.warning("Security exception: %s", exc)
    return JSONResponse(status_code=403, content=_error_body(403, str(exc), request))


@app.exception_handler(InvalidOperationException)
async def invalid_operation_handler(request: Request, exc: InvalidOperationException):
    log.warning("Invalid operation: %s", exc)
    return JSONResponse(status_code=400, content=_error_body(400, str(exc), request))


@app.exception_handler(ValueError)
async def value_error_handler(request: Request, exc: ValueError):
    log.warning("Illegal argument: %s", exc)
    return JSONResponse(status_code=400, content=_error_body(400, str(exc), request))


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    log.exception("Unexpected error")
    message = f"An unexpected error occurred: {exc} | {type(exc).__module__}.{type(exc).__name__}"
    return JSONResponse(status_code=500, content=_error_body(500, message, request))


import os

os.makedirs(settings.upload_dir, exist_ok=True)
app.mount("/uploads", StaticFiles(directory=settings.upload_dir), name="uploads")


@app.on_event("startup")
def on_startup():
    from app.core.database import Base, engine
    import app.models  # noqa: F401 ensures all models are registered on Base.metadata
    from app.bootstrap import bootstrap_super_admins, bootstrap_permissions
    from app.scheduler import start_scheduler

    Base.metadata.create_all(bind=engine)
    bootstrap_super_admins()
    bootstrap_permissions()
    start_scheduler()
    log.info("Fitnexus FastAPI backend started on port %s", settings.server_port)


from app.api.routers import auth as auth_router
from app.api.routers import permissions as permissions_router
from app.api.routers import org_hierarchy as org_hierarchy_router
from app.api.routers import user_management as user_management_router
from app.api.routers import membership_plans as membership_plans_router
from app.api.routers import membership as membership_router
from app.api.routers import billing as billing_router
from app.api.routers import attendance as attendance_router
from app.api.routers import notifications as notifications_router

app.include_router(auth_router.router)
app.include_router(permissions_router.router)
app.include_router(org_hierarchy_router.router)
app.include_router(user_management_router.router)
app.include_router(membership_plans_router.router)
app.include_router(membership_router.router)
app.include_router(billing_router.router)
app.include_router(attendance_router.router)
app.include_router(notifications_router.router)
