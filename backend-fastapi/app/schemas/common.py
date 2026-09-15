from datetime import datetime
from typing import Generic, TypeVar

from pydantic import BaseModel

T = TypeVar("T")


class ApiSuccessResponse(BaseModel, Generic[T]):
    status: int
    message: str
    data: T | None = None
    timestamp: str = ""

    def __init__(self, **data):
        data.setdefault("timestamp", datetime.now().isoformat())
        super().__init__(**data)


class ApiErrorResponse(BaseModel):
    status: int
    message: str
    timestamp: str
    path: str


class ValidationErrorResponse(ApiErrorResponse):
    fieldErrors: dict[str, str]


def success(data=None, message: str = "Success", status: int = 200) -> dict:
    return {
        "status": status,
        "message": message,
        "data": data,
        "timestamp": datetime.now().isoformat(),
    }
