from datetime import date, datetime

from sqlalchemy import Date, DateTime, ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class _HrProfileMixin:
    """Shared personal/employment/bank/document/family/reference fields common to
    Admin, Manager and Trainer in the Java model (identical field lists there)."""

    date_of_birth: Mapped[date | None] = mapped_column(Date)
    gender: Mapped[str | None] = mapped_column(String(30))
    blood_group: Mapped[str | None] = mapped_column(String(5))
    personal_email: Mapped[str | None] = mapped_column(String(100))
    alternate_phone: Mapped[str | None] = mapped_column(String(20))
    emergency_contact: Mapped[str | None] = mapped_column(String(100))
    emergency_phone: Mapped[str | None] = mapped_column(String(20))
    current_address: Mapped[str | None] = mapped_column(String(300))
    permanent_address: Mapped[str | None] = mapped_column(String(300))
    city: Mapped[str | None] = mapped_column(String(80))
    state: Mapped[str | None] = mapped_column(String(80))
    pincode: Mapped[str | None] = mapped_column(String(20))

    employment_type: Mapped[str | None] = mapped_column(String(50))
    work_location: Mapped[str | None] = mapped_column(String(100))
    reporting_manager_name: Mapped[str | None] = mapped_column(String(100))
    probation_end_date: Mapped[date | None] = mapped_column(Date)
    pan_number: Mapped[str | None] = mapped_column(String(20))
    aadhar_number: Mapped[str | None] = mapped_column(String(20))

    bank_name: Mapped[str | None] = mapped_column(String(100))
    bank_account_number: Mapped[str | None] = mapped_column(String(40))
    bank_ifsc_code: Mapped[str | None] = mapped_column(String(20))
    bank_account_type: Mapped[str | None] = mapped_column(String(30))
    bank_account_holder_name: Mapped[str | None] = mapped_column(String(100))
    bank_branch: Mapped[str | None] = mapped_column(String(150))
    bank_document_path: Mapped[str | None] = mapped_column(String(500))

    qualification_document_path: Mapped[str | None] = mapped_column(String(500))
    certification_document_path: Mapped[str | None] = mapped_column(String(500))
    id_proof_document_path: Mapped[str | None] = mapped_column(String(500))
    address_proof_document_path: Mapped[str | None] = mapped_column(String(500))
    resume_document_path: Mapped[str | None] = mapped_column(String(500))
    offer_letter_document_path: Mapped[str | None] = mapped_column(String(500))
    candidate_photo_path: Mapped[str | None] = mapped_column(String(500))
    aadhar_card_document_path: Mapped[str | None] = mapped_column(String(500))
    pan_card_document_path: Mapped[str | None] = mapped_column(String(500))
    experience_certificate_document_path: Mapped[str | None] = mapped_column(String(500))
    course_certificate_path: Mapped[str | None] = mapped_column(String(500))
    education_certificate_path: Mapped[str | None] = mapped_column(String(500))

    father_name: Mapped[str | None] = mapped_column(String(100))
    mother_name: Mapped[str | None] = mapped_column(String(100))
    marital_status: Mapped[str | None] = mapped_column(String(30))
    spouse_name: Mapped[str | None] = mapped_column(String(100))
    location: Mapped[str | None] = mapped_column(String(100))

    emergency_contact_relationship: Mapped[str | None] = mapped_column(String(80))
    emergency_contact_name2: Mapped[str | None] = mapped_column(String(100))
    emergency_contact_relationship2: Mapped[str | None] = mapped_column(String(80))
    emergency_phone2: Mapped[str | None] = mapped_column(String(20))
    reference_name1: Mapped[str | None] = mapped_column(String(100))
    reference_phone1: Mapped[str | None] = mapped_column(String(20))
    reference_name2: Mapped[str | None] = mapped_column(String(100))
    reference_phone2: Mapped[str | None] = mapped_column(String(20))

    previous_employment1: Mapped[str | None] = mapped_column(String(500))
    previous_employment2: Mapped[str | None] = mapped_column(String(500))

    joining_branch_name: Mapped[str | None] = mapped_column(String(100))
    source_platform: Mapped[str | None] = mapped_column(String(80))
    pf_uan: Mapped[str | None] = mapped_column(String(30))
    esi_number: Mapped[str | None] = mapped_column(String(30))
    declaration_date: Mapped[date | None] = mapped_column(Date)
    declaration_place: Mapped[str | None] = mapped_column(String(100))


class Admin(Base, _HrProfileMixin):
    __tablename__ = "admins"

    id: Mapped[int] = mapped_column("user_id", ForeignKey("user_accounts.id"), primary_key=True)
    account: Mapped["Users"] = relationship("Users", foreign_keys=[id])

    first_name: Mapped[str] = mapped_column(String(50), nullable=False)
    last_name: Mapped[str | None] = mapped_column(String(50))
    department: Mapped[str] = mapped_column(String(100), nullable=False)
    phone: Mapped[str] = mapped_column(String(15), nullable=False)
    employee_id: Mapped[str] = mapped_column(String(20), unique=True, nullable=False)
    qualification: Mapped[str] = mapped_column(String(100), nullable=False)
    join_date: Mapped[datetime | None] = mapped_column(DateTime, default=datetime.now)
    bio: Mapped[str | None] = mapped_column(String(500))
    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, default=datetime.now)
    updated_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, default=datetime.now, onupdate=datetime.now)


class Manager(Base, _HrProfileMixin):
    __tablename__ = "managers"

    id: Mapped[int] = mapped_column("user_id", ForeignKey("user_accounts.id"), primary_key=True)
    account: Mapped["Users"] = relationship("Users", foreign_keys=[id])

    first_name: Mapped[str] = mapped_column(String(50), nullable=False)
    last_name: Mapped[str | None] = mapped_column(String(50))
    department: Mapped[str] = mapped_column(String(100), nullable=False)
    phone: Mapped[str] = mapped_column(String(15), nullable=False)
    employee_id: Mapped[str] = mapped_column(String(20), unique=True, nullable=False)
    qualification: Mapped[str] = mapped_column(String(100), nullable=False)
    join_date: Mapped[datetime | None] = mapped_column(DateTime, default=datetime.now)
    bio: Mapped[str | None] = mapped_column(String(500))
    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, default=datetime.now)
    updated_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, default=datetime.now, onupdate=datetime.now)


class Trainer(Base, _HrProfileMixin):
    __tablename__ = "trainers"

    id: Mapped[int] = mapped_column("user_id", ForeignKey("user_accounts.id"), primary_key=True)
    account: Mapped["Users"] = relationship("Users", foreign_keys=[id])

    first_name: Mapped[str] = mapped_column(String(50), nullable=False)
    last_name: Mapped[str | None] = mapped_column(String(50))
    specialization: Mapped[str] = mapped_column(String(100), nullable=False)
    experience_years: Mapped[int] = mapped_column(nullable=False)
    certification: Mapped[str] = mapped_column(String(100), nullable=False)
    phone: Mapped[str] = mapped_column(String(15), nullable=False)
    qualification: Mapped[str] = mapped_column(String(100), nullable=False)
    rate_per_hour: Mapped[float] = mapped_column(nullable=False)
    bio: Mapped[str | None] = mapped_column(String(500))
    languages: Mapped[str | None] = mapped_column(String(200))
    rating: Mapped[float] = mapped_column(default=0.0)
    total_clients_trained: Mapped[int] = mapped_column(default=0)
    join_date: Mapped[datetime | None] = mapped_column(DateTime, default=datetime.now)
    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, default=datetime.now)
    updated_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, default=datetime.now, onupdate=datetime.now)


class Counselor(Base):
    __tablename__ = "counselors"

    id: Mapped[int] = mapped_column("user_id", ForeignKey("user_accounts.id"), primary_key=True)
    account: Mapped["Users"] = relationship("Users", foreign_keys=[id])

    first_name: Mapped[str] = mapped_column(String(50), nullable=False)
    last_name: Mapped[str | None] = mapped_column(String(50))
    phone: Mapped[str] = mapped_column(String(15), nullable=False)
    join_date: Mapped[datetime | None] = mapped_column(DateTime, default=datetime.now)
    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, default=datetime.now)
    updated_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, default=datetime.now, onupdate=datetime.now)


# Field names shared by Admin/Manager/Trainer, in the exact order the Java "professional
# fields" copy step sets them — used by the service layer's generic field-copy helper.
HR_PROFILE_FIELDS = [
    "date_of_birth", "gender", "blood_group", "personal_email", "alternate_phone",
    "emergency_contact", "emergency_phone", "current_address", "permanent_address",
    "city", "state", "pincode", "employment_type", "work_location", "reporting_manager_name",
    "probation_end_date", "pan_number", "aadhar_number", "bank_name", "bank_account_number",
    "bank_ifsc_code", "bank_account_type", "bank_account_holder_name", "bank_branch",
    "bank_document_path", "qualification_document_path", "certification_document_path",
    "id_proof_document_path", "address_proof_document_path", "resume_document_path",
    "offer_letter_document_path", "candidate_photo_path", "aadhar_card_document_path",
    "pan_card_document_path", "experience_certificate_document_path", "course_certificate_path",
    "education_certificate_path", "father_name", "mother_name", "marital_status", "spouse_name",
    "location", "emergency_contact_relationship", "emergency_contact_name2",
    "emergency_contact_relationship2", "emergency_phone2", "reference_name1", "reference_phone1",
    "reference_name2", "reference_phone2", "previous_employment1", "previous_employment2",
    "joining_branch_name", "source_platform", "pf_uan", "esi_number", "declaration_date",
    "declaration_place",
]
