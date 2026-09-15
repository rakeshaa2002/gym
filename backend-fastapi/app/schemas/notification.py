from pydantic import BaseModel


class NotificationResponse(BaseModel):
    key: str
    type: str
    title: str
    message: str | None
    time: str
    link: str | None
