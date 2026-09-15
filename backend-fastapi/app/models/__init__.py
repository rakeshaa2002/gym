from app.models.users import Users  # noqa: F401
from app.models.fitness_user import FitnessUser  # noqa: F401
from app.models.super_admin import SuperAdmin  # noqa: F401
from app.models.otp import EmailOtp  # noqa: F401
from app.models.permission import PermissionPage, RolePagePermission  # noqa: F401
from app.models.org import HeadOffice, Branch, Department, Designation, Team  # noqa: F401
from app.models.staff import Admin, Manager, Trainer, Counselor  # noqa: F401
from app.models.notification import Notification  # noqa: F401
from app.models.membership import MembershipPlan, MembershipRequest, Transaction  # noqa: F401
from app.models.attendance import Attendance  # noqa: F401
from app.models.fitness import Goal, ProgressEntry, UserWorkoutSchedule  # noqa: F401
from app.models.chat import ChatMessage  # noqa: F401
