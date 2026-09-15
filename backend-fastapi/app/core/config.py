from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    db_host: str = "localhost"
    db_port: int = 5432
    db_name: str = "fitnexus"
    db_username: str = "postgres"
    db_password: str = "postgres"

    server_port: int = 8082
    jwt_secret: str = "ASDFLKJGHQWERTYPOIUYMNBZXCVASDFLKJGHQWERTY"
    jwt_expiration_hours: int = 3

    super_admin_email: str = "admin@fitnexus.test"
    super_admin_secondary_email: str = "admin@gmail.com"
    super_admin_first_name: str = "Admin"
    super_admin_last_name: str = ""
    super_admin_password: str = "Admin@123"

    membership_premium_price: int = 999

    mail_host: str = ""
    mail_port: int = 587
    mail_username: str = ""
    mail_password: str = ""
    mail_from: str = "no-reply@fitnexus.test"
    otp_expiry_minutes: int = 10

    webrtc_stun_server: str = "stun:stun.l.google.com:19302"
    webrtc_turn_url: str = ""
    webrtc_turn_username: str = ""
    webrtc_turn_credential: str = ""

    razorpay_key_id: str = ""
    razorpay_key_secret: str = ""

    attendance_overstay_check_ms: int = 300000
    attendance_device_key: str = "fitnexus-device-key-2026"
    attendance_access_grace_minutes: int = 0

    upload_dir: str = "uploads"

    cors_origins: list[str] = [
        "http://localhost:5173",
        "http://localhost:3000",
        "http://gym.infitoolz.com",
        "https://gym.infitoolz.com",
    ]

    @property
    def database_url(self) -> str:
        return (
            f"postgresql+psycopg2://{self.db_username}:{self.db_password}"
            f"@{self.db_host}:{self.db_port}/{self.db_name}"
        )


settings = Settings()
