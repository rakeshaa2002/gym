import enum


class Role(str, enum.Enum):
    SUPER_ADMIN = "SUPER_ADMIN"
    ADMIN = "ADMIN"
    TRAINER = "TRAINER"
    MANAGER = "MANAGER"
    USER = "USER"
    CORPORATE_HR = "CORPORATE_HR"
    COUNSELOR = "COUNSELOR"
