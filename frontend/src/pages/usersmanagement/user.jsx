import React, { useEffect, useMemo, useRef, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { Modal, Button, Form } from "react-bootstrap";
import { IconHome, IconEdit, IconTrash, IconPlus } from "@tabler/icons-react";
import api from "../../utils/api";
import {
  getAllHeadOffices,
  getAllBranches,
  getAllDepartments,
  getAllTeams,
  getAllDesignations,
} from "../../api/orgHierarchyApi";
import { extractApiErrorMessage } from "../../utils/errorMessage";
import {
  COUNTRY_CODE_OPTIONS,
  ensureCountryCodeValue,
  getCountryAllowedLengths,
  getCountryOptionByValue,
  sanitizePhoneDigits,
  validatePhoneNumber,
} from "../../utils/phoneUtils";
import { useAuth } from "../../context/AuthContext";
import adminAvatar from "/src/assets/images/avtar/profile.png";
import superAdminAvatar from "/src/assets/images/avtar/profile-img.png";
import userAvatar from "/src/assets/images/avtar/samantha-lee.png";
import managerAvatar from "/src/assets/images/trainer/trainer1-avtar.png";
import trainerAvatar from "/src/assets/images/trainer/trainer2-avtar.png";

const ROLE_OPTIONS = [
  { value: "SUPER_ADMIN", label: "Super Admin" },
  { value: "ADMIN", label: "Admin" },
  { value: "MANAGER", label: "Manager" },
  { value: "TRAINER", label: "Trainer" },
  { value: "USER", label: "User" },
];

const ROLE_COLORS = {
  SUPER_ADMIN: "bg-dark",
  ADMIN: "bg-primary",
  MANAGER: "bg-info",
  TRAINER: "bg-success",
  USER: "bg-secondary",
};

const GENDER_OPTIONS = ["Male", "Female", "Non-binary", "Prefer not to say"];
const BLOOD_GROUP_OPTIONS = ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];
const EMPLOYMENT_TYPE_OPTIONS = ["Full-time", "Part-time", "Contract", "Intern", "Consultant"];
const WORK_LOCATION_OPTIONS = ["On-site", "Remote", "Hybrid", "Branch-based"];
const QUALIFICATION_OPTIONS = ["SSLC", "HSC", "Diploma", "UG", "PG", "Doctorate", "Professional Certification", "Others"];
const MARITAL_STATUS_OPTIONS = ["Single", "Married"];
const SOURCE_PLATFORM_OPTIONS = ["Friend Refer", "Local App", "Direct Interview", "Job Fair"];
const EMPTY_ADDRESS = { street: "", city: "", state: "", pincode: "" };
const STAFF_PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$/;

const ROLE_AVATARS = {
  SUPER_ADMIN: superAdminAvatar,
  ADMIN: adminAvatar,
  MANAGER: managerAvatar,
  TRAINER: trainerAvatar,
  USER: userAvatar,
};

function normalizeRole(role) {
  return String(role || "").trim().toUpperCase();
}

function getCurrentUserId(user) {
  const raw = user?.userId ?? user?.id ?? localStorage.getItem("userId");
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

function splitPhoneWithCountryCode(value) {
  const raw = String(value || "").trim();
  if (!raw) return { countryCode: "+91", phone: "" };
  if (!raw.startsWith("+")) return { countryCode: "+91", phone: raw.replace(/\D/g, "") };

  const match = [...COUNTRY_CODE_OPTIONS]
    .sort((a, b) => b.value.length - a.value.length)
    .find((opt) => raw.startsWith(opt.value));

  if (match) {
    return {
      countryCode: match.value,
      phone: raw.slice(match.value.length).replace(/\D/g, ""),
    };
  }

  return { countryCode: "+91", phone: raw.replace(/\D/g, "") };
}

function formatPhoneWithCode(value, code = "+91") {
  const phone = String(value || "").trim();
  if (!phone) return "-";
  if (phone.startsWith("+")) return phone;
  return `${ensureCountryCodeValue(code)}${phone}`;
}

function toDateInputValue(value) {
  if (!value) return "";
  return String(value).slice(0, 10);
}

function unwrapData(response) {
  return response?.data?.data ?? null;
}

function isActiveRecord(item) {
  const status = String(item?.status || "").toUpperCase();
  if (!status) return true;
  return status === "ACTIVE";
}

function getDefaultCreateRole(currentRole) {
  switch (normalizeRole(currentRole)) {
    case "SUPER_ADMIN":
      return "ADMIN";
    case "ADMIN":
      return "MANAGER";
    case "MANAGER":
      return "TRAINER";
    case "TRAINER":
      return "USER";
    default:
      return "ADMIN";
  }
}

function getCreateRoleAvailability(currentRole) {
  const role = normalizeRole(currentRole);
  const availability = {
    SUPER_ADMIN: false,
    ADMIN: false,
    MANAGER: false,
    TRAINER: false,
    USER: false,
  };

  if (role === "SUPER_ADMIN") {
    availability.ADMIN = true;
    availability.MANAGER = true;
    availability.TRAINER = true;
    availability.USER = true;
  } else if (role === "ADMIN") {
    availability.MANAGER = true;
    availability.TRAINER = true;
    availability.USER = true;
  } else if (role === "MANAGER") {
    availability.TRAINER = true;
    availability.USER = true;
  } else if (role === "TRAINER") {
    availability.USER = true;
  }

  return availability;
}

function createEmptyForm(role = "ADMIN") {
  return {
    role,
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    name: "",
    phone: "",
    countryCode: "+91",
    employeeCode: "",
    departmentText: "",
    qualification: "",
    specialization: "",
    experienceYears: "",
    certification: "",
    ratePerHour: "",
    weight: "",
    height: "",
    bloodGroup: "",
    age: "",
    gender: "",
    address: "",
    city: "",
    medicalConditions: "",
    emergencyContact: "",
    emergencyPhone: "",
    status: "ACTIVE",
    joinDate: "",
    bio: "",
    languages: "",
    rating: "",
    totalClientsTrained: "",
    headOfficeId: "",
    branchId: "",
    departmentId: "",
    teamId: "",
    designationId: "",
    permanentAddress: "",
    currentAddress: "",
    dateOfBirth: "",
    state: "",
    pincode: "",
    employmentType: "",
    workLocation: "",
    reportingManagerName: "",
    reportsToId: "",
    fatherName: "",
    motherName: "",
    maritalStatus: "",
    spouseName: "",
    location: "",
    probationEndDate: "",
    alternatePhone: "",
    personalEmail: "",
    panNumber: "",
    aadharNumber: "",
    bankName: "",
    bankAccountNumber: "",
    bankIfscCode: "",
    bankAccountType: "",
    bankAccountHolderName: "",
    bankBranch: "",
    qualificationDocumentPath: "",
    certificationDocumentPath: "",
    idProofDocumentPath: "",
    addressProofDocumentPath: "",
    resumeDocumentPath: "",
    offerLetterDocumentPath: "",
    candidatePhotoPath: "",
    aadharCardDocumentPath: "",
    panCardDocumentPath: "",
    bankDocumentPath: "",
    previousEmployment1: "",
    previousEmployment2: "",
    experienceCertificateDocumentPath: "",
    courseCertificatePath: "",
    educationCertificatePath: "",
    emergencyContactRelationship: "",
    emergencyContactName2: "",
    emergencyContactRelationship2: "",
    emergencyPhone2: "",
    referenceName1: "",
    referencePhone1: "",
    referenceName2: "",
    referencePhone2: "",
    joiningBranchName: "",
    sourcePlatform: "",
    pfUan: "",
    esiNumber: "",
    declarationDate: "",
    declarationPlace: "",
    img: ROLE_AVATARS[normalizeRole(role)] || userAvatar,
  };
}

function splitName(fullName = "") {
  const normalized = String(fullName)
    .trim()
    .replace(/\s+/g, " ");

  if (!normalized) {
    return { firstName: "", lastName: "" };
  }

  const parts = normalized.split(" ");
  return {
    firstName: parts[0],
    lastName: parts.slice(1).join(" "),
  };
}

function buildOrgLabel(item, orgLists) {
  if (!item) return "-";
  const parts = [];

  const headOfficeId = item.headOfficeId ?? item.headOffice?.id;
  const branchId = item.branchId ?? item.branch?.id;
  const departmentId = item.departmentId ?? item.department?.id;
  const teamId = item.teamId ?? item.team?.id;
  const designationId = item.designationId ?? item.designation?.id;

  if (headOfficeId) {
    const headOffice = orgLists.headOffices.find((entry) => String(entry.id) === String(headOfficeId));
    if (headOffice?.name) parts.push(headOffice.name);
  }

  if (branchId) {
    const branch = orgLists.branches.find((entry) => String(entry.id) === String(branchId));
    if (branch?.name) parts.push(branch.name);
  }

  if (departmentId) {
    const department = orgLists.departments.find((entry) => String(entry.id) === String(departmentId));
    if (department?.name) parts.push(department.name);
  }

  if (teamId) {
    const team = orgLists.teams.find((entry) => String(entry.id) === String(teamId));
    if (team?.name) parts.push(team.name);
  }

  if (designationId) {
    const designation = orgLists.designations.find((entry) => String(entry.id) === String(designationId));
    if (designation?.name) parts.push(designation.name);
  }

  return parts.length ? parts.join(" > ") : item.department || item.branch || item.designation || "-";
}

function mapAdminRow(item) {
  const { countryCode, phone } = splitPhoneWithCountryCode(item?.phone);
  return {
    id: item?.id ?? null,
    role: "ADMIN",
    name: [item?.firstName, item?.lastName].filter(Boolean).join(" "),
    email: item?.email || "",
    phone,
    countryCode,
    employeeCode: item?.employeeId || "",
    department: item?.department || "",
    qualification: item?.qualification || "",
    status: item?.isActive ? "ACTIVE" : "INACTIVE",
    createdByName: item?.createdByName || "",
    img: ROLE_AVATARS.ADMIN,
    raw: item,
  };
}

function mapSuperAdminRow(item) {
  return {
    id: item?.id ?? null,
    role: "SUPER_ADMIN",
    name: [item?.firstName, item?.lastName].filter(Boolean).join(" "),
    email: item?.email || "",
    phone: "",
    countryCode: "+91",
    employeeCode: "",
    department: "Super Admin",
    qualification: "",
    status: item?.isActive ? "ACTIVE" : "INACTIVE",
    createdByName: "",
    img: ROLE_AVATARS.SUPER_ADMIN,
    raw: item,
  };
}

function mapManagerRow(item) {
  const row = mapAdminRow(item);
  return {
    ...row,
    role: "MANAGER",
  img: ROLE_AVATARS.MANAGER,
  };
}

function mapTrainerRow(item) {
  const { countryCode, phone } = splitPhoneWithCountryCode(item?.phone);
  return {
    id: item?.id ?? null,
    role: "TRAINER",
    name: [item?.firstName, item?.lastName].filter(Boolean).join(" "),
    email: item?.email || "",
    phone,
    countryCode,
    employeeCode: "",
    department: item?.specialization || "",
    qualification: item?.qualification || "",
    status: item?.isActive ? "ACTIVE" : "INACTIVE",
    createdByName: item?.createdByName || "",
    img: ROLE_AVATARS.TRAINER,
    raw: item,
  };
}

function mapCustomerRow(item) {
  const { countryCode, phone } = splitPhoneWithCountryCode(item?.phone);
  return {
    id: item?.id ?? null,
    role: "USER",
    name: [item?.firstName, item?.lastName].filter(Boolean).join(" "),
    email: item?.email || "",
    phone,
    countryCode,
    employeeCode: "",
    department: item?.assignedTrainerName || "",
    qualification: "",
    status: item?.isActive ? "ACTIVE" : "INACTIVE",
    createdByName: item?.assignedTrainerName || "",
    img: ROLE_AVATARS.USER,
    raw: item,
  };
}

function getCountryOptions(search) {
  const term = String(search || "").trim().toLowerCase();
  const base = [...COUNTRY_CODE_OPTIONS].sort((a, b) => String(a.label).localeCompare(String(b.label)));
  if (!term) return base;
  return base.filter(
    (opt) => String(opt.label).toLowerCase().includes(term) || String(opt.value).toLowerCase().includes(term),
  );
}

function getRoleBadgeClass(role) {
  return ROLE_COLORS[normalizeRole(role)] || "bg-secondary";
}

function toSafeId(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

function getActiveByIds(list) {
  return (Array.isArray(list) ? list : []).filter(isActiveRecord);
}

function SectionHeader({ label }) {
  return (
    <div className="col-12 mt-4">
      <h6 className="mb-3 text-primary">{label}</h6>
    </div>
  );
}

function StatCard({ value, label }) {
  return (
    <div className="col-md-3">
      <div className="card h-100">
        <div className="card-body text-center">
          <h3 className="mb-0">{value}</h3>
          <p className="text-muted small mb-0">{label}</p>
        </div>
      </div>
    </div>
  );
}

export default function User() {
  const location = useLocation();
  const { user: currentUser } = useAuth();

  const currentRole = normalizeRole(currentUser?.role);
  const currentUserId = getCurrentUserId(currentUser);
  const gridView = location.pathname.endsWith("/users-grid");
  const routeViewMode = location.pathname.endsWith("/employees") ? "employees" : "users";

  const [rows, setRows] = useState([]);
  const [viewMode, setViewMode] = useState(routeViewMode);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [saving, setSaving] = useState(false);
  const [uploadingField, setUploadingField] = useState("");

  const [showModal, setShowModal] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [selectedRow, setSelectedRow] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [modalError, setModalError] = useState("");
  const [modalTab, setModalTab] = useState("identity");

  const [form, setForm] = useState(createEmptyForm(getDefaultCreateRole(currentRole)));

  useEffect(() => {
    setViewMode(routeViewMode);
  }, [routeViewMode]);
  const [orgLoading, setOrgLoading] = useState(false);
  const [headOffices, setHeadOffices] = useState([]);
  const [branches, setBranches] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [teams, setTeams] = useState([]);
  const [designations, setDesignations] = useState([]);
  const [reportingOptions, setReportingOptions] = useState([]);
  const [reportingOptionsLoading, setReportingOptionsLoading] = useState(false);

  const [countryDropdownOpen, setCountryDropdownOpen] = useState(false);
  const [countrySearch, setCountrySearch] = useState("");
  const countryDropdownRef = useRef(null);

  const createAvailability = useMemo(() => getCreateRoleAvailability(currentRole), [currentRole]);
  const canCreateAnything = useMemo(() => Object.values(createAvailability).some(Boolean), [createAvailability]);
  const canCreateInCurrentView = useMemo(() => {
    if (viewMode === "users") return Boolean(createAvailability.USER);
    return ROLE_OPTIONS.some((item) => item.value !== "USER" && createAvailability[item.value]);
  }, [createAvailability, viewMode]);

  const creatableRolesForCurrentView = useMemo(() => {
    return ROLE_OPTIONS.filter((item) => {
      if (!createAvailability[item.value]) return false;
      return viewMode === "users" ? item.value === "USER" : item.value !== "USER";
    });
  }, [createAvailability, viewMode]);

  const selectedHeadOffice = useMemo(
    () => headOffices.find((item) => String(item.id) === String(form.headOfficeId)) || null,
    [headOffices, form.headOfficeId],
  );

  const selectedBranch = useMemo(
    () => branches.find((item) => String(item.id) === String(form.branchId)) || null,
    [branches, form.branchId],
  );

  const selectedDepartment = useMemo(
    () => departments.find((item) => String(item.id) === String(form.departmentId)) || null,
    [departments, form.departmentId],
  );

  const selectedTeam = useMemo(
    () => teams.find((item) => String(item.id) === String(form.teamId)) || null,
    [teams, form.teamId],
  );

  const selectedDesignation = useMemo(
    () => designations.find((item) => String(item.id) === String(form.designationId)) || null,
    [designations, form.designationId],
  );

  const filteredBranches = useMemo(() => {
    const active = getActiveByIds(branches);
    if (!form.headOfficeId) return active;
    return active.filter((item) => String(item.headOfficeId) === String(form.headOfficeId));
  }, [branches, form.headOfficeId]);

  const filteredDepartments = useMemo(() => {
    const active = getActiveByIds(departments);
    if (!form.branchId) return active.filter(Boolean);
    return active.filter((item) => String(item.branchId) === String(form.branchId));
  }, [departments, form.branchId]);

  const filteredTeams = useMemo(() => {
    const active = getActiveByIds(teams);
    if (!form.departmentId) return active.filter(Boolean);
    return active.filter((item) => String(item.departmentId) === String(form.departmentId));
  }, [teams, form.departmentId]);

  const filteredDesignations = useMemo(() => {
    const active = getActiveByIds(designations);
    if (!form.departmentId) return active.filter(Boolean);
    return active.filter((item) => String(item.departmentId) === String(form.departmentId));
  }, [designations, form.departmentId]);

  const orgPreview = useMemo(() => {
    const parts = [
      selectedHeadOffice?.name,
      selectedBranch?.name,
      selectedDepartment?.name,
      selectedTeam?.name,
      selectedDesignation?.name,
    ].filter(Boolean);

    return parts.length ? parts.join(" > ") : "Not assigned yet";
  }, [selectedHeadOffice, selectedBranch, selectedDepartment, selectedTeam, selectedDesignation]);

  const employeeRows = useMemo(() => rows.filter((item) => normalizeRole(item.role) !== "USER"), [rows]);
  const userRows = useMemo(() => rows.filter((item) => normalizeRole(item.role) === "USER"), [rows]);
  const displayedRows = useMemo(() => (viewMode === "users" ? userRows : employeeRows), [employeeRows, userRows, viewMode]);

  const activeRows = useMemo(
    () => displayedRows.filter((item) => String(item.status).toUpperCase() === "ACTIVE"),
    [displayedRows],
  );
  const inactiveRows = useMemo(
    () => displayedRows.filter((item) => String(item.status).toUpperCase() !== "ACTIVE"),
    [displayedRows],
  );

  const roleCounts = useMemo(() => {
    return displayedRows.reduce((acc, item) => {
      const key = normalizeRole(item.role);
      acc[key] = (acc[key] || 0) + 1;
      return acc;
    }, {});
  }, [displayedRows]);

  const totalRoles = useMemo(() => Object.keys(roleCounts).length, [roleCounts]);
  const loadOrgData = async () => {
    setOrgLoading(true);
    try {
      const [ho, br, dep, tm, des] = await Promise.all([
        getAllHeadOffices(),
        getAllBranches(),
        getAllDepartments(),
        getAllTeams(),
        getAllDesignations(),
      ]);

      setHeadOffices(getActiveByIds(ho));
      setBranches(getActiveByIds(br));
      setDepartments(getActiveByIds(dep));
      setTeams(getActiveByIds(tm));
      setDesignations(getActiveByIds(des));
    } catch (e) {
      setModalError(extractApiErrorMessage(e, "Failed to load organization masters"));
    } finally {
      setOrgLoading(false);
    }
  };

  const loadRows = async () => {
    if (!currentUserId) return;

    setLoading(true);
    setError("");

    try {
      const nextRows = [];

      if (currentRole === "SUPER_ADMIN") {
        const [adminsResponse, managersResponse, trainersResponse, customersResponse] = await Promise.all([
          api.get("/users/admins", { params: { requesterId: currentUserId } }),
          api.get("/users/managers", { params: { requesterId: currentUserId } }),
          api.get("/users/trainers", { params: { requesterId: currentUserId } }),
          api.get("/users/customers", { params: { requesterId: currentUserId } }),
        ]);

        const admins = unwrapData(adminsResponse);
        const managers = unwrapData(managersResponse);
        const trainers = unwrapData(trainersResponse);
        const customers = unwrapData(customersResponse);

        if (Array.isArray(admins)) nextRows.push(...admins.map(mapAdminRow));
        if (Array.isArray(managers)) nextRows.push(...managers.map(mapManagerRow));
        if (Array.isArray(trainers)) nextRows.push(...trainers.map(mapTrainerRow));
        if (Array.isArray(customers)) nextRows.push(...customers.map(mapCustomerRow));
      } else if (currentRole === "ADMIN") {
        const [managersResponse, trainersResponse, customersResponse] = await Promise.all([
          api.get("/users/managers", { params: { requesterId: currentUserId } }),
          api.get("/users/trainers", { params: { requesterId: currentUserId } }),
          api.get("/users/customers", { params: { requesterId: currentUserId } }),
        ]);
        const managers = unwrapData(managersResponse);
        const trainers = unwrapData(trainersResponse);
        const customers = unwrapData(customersResponse);
        if (Array.isArray(managers)) nextRows.push(...managers.map(mapManagerRow));
        if (Array.isArray(trainers)) nextRows.push(...trainers.map(mapTrainerRow));
        if (Array.isArray(customers)) nextRows.push(...customers.map(mapCustomerRow));
      } else if (currentRole === "MANAGER") {
        const [trainersResponse, customersResponse] = await Promise.all([
          api.get("/users/trainers", { params: { requesterId: currentUserId } }),
          api.get("/users/customers", { params: { requesterId: currentUserId } }),
        ]);
        const trainers = unwrapData(trainersResponse);
        const customers = unwrapData(customersResponse);
        if (Array.isArray(trainers)) nextRows.push(...trainers.map(mapTrainerRow));
        if (Array.isArray(customers)) nextRows.push(...customers.map(mapCustomerRow));
      } else if (currentRole === "TRAINER") {
        const customersResponse = await api.get(`/users/customers/assigned-to/${currentUserId}`);
        const customers = unwrapData(customersResponse);
        if (Array.isArray(customers)) nextRows.push(...customers.map(mapCustomerRow));
      } else if (currentRole === "USER") {
        const customerResponse = await api.get(`/users/customer/${currentUserId}`, {
          params: { requesterId: currentUserId },
        });
        const customer = unwrapData(customerResponse);
        if (customer) nextRows.push(mapCustomerRow(customer));
      }

      nextRows.sort((a, b) => String(a.role).localeCompare(String(b.role)) || String(a.name).localeCompare(String(b.name)));
      setRows(nextRows);
    } catch (e) {
      setRows([]);
      setError(extractApiErrorMessage(e, "Failed to load users"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrgData();
  }, []);

  useEffect(() => {
    loadRows();
  }, [currentUserId, currentRole]);
  useEffect(() => {
    const role = normalizeRole(isEdit ? selectedRow?.role : form.role);
    if (!showModal || !currentUserId || !["ADMIN", "MANAGER", "TRAINER"].includes(role)) {
      setReportingOptions([]);
      return;
    }

    let cancelled = false;
    const loadReportingOptions = async () => {
      setReportingOptionsLoading(true);
      try {
        const response = await api.get("/users/reporting-options", {
          params: {
            role,
            branchId: toSafeId(form.branchId),
            requesterId: currentUserId,
          },
        });
        if (!cancelled) {
          const options = unwrapData(response);
          setReportingOptions(Array.isArray(options) ? options : []);
        }
      } catch (err) {
        if (!cancelled) setReportingOptions([]);
      } finally {
        if (!cancelled) setReportingOptionsLoading(false);
      }
    };

    loadReportingOptions();
    return () => {
      cancelled = true;
    };
  }, [showModal, isEdit, selectedRow?.role, form.role, form.branchId, currentUserId]);

  useEffect(() => {
    if (!notice) return;
    const timer = setTimeout(() => setNotice(""), 2500);
    return () => clearTimeout(timer);
  }, [notice]);

  useEffect(() => {
    const handleOutside = (event) => {
      if (countryDropdownRef.current && !countryDropdownRef.current.contains(event.target)) {
        setCountryDropdownOpen(false);
      }
    };

    document.addEventListener("mousedown", handleOutside);
    return () => document.removeEventListener("mousedown", handleOutside);
  }, []);

  const openAdd = () => {
    if (!canCreateInCurrentView) {
      setError(`Your current role cannot create ${viewMode === "users" ? "users" : "employees"} from this page.`);
      return;
    }

    const defaultRole = viewMode === "users" && createAvailability.USER ? "USER" : getDefaultCreateRole(currentRole);
    setForm(createEmptyForm(defaultRole));
    setSelectedRow(null);
    setIsEdit(false);
    setModalError("");
    setModalTab("identity");
    setCountrySearch("");
    setCountryDropdownOpen(false);
    setShowModal(true);
  };

  const openEdit = (row) => {
    const split = splitName(row?.name);
    const org = row?.raw || {};

    setForm({
      ...createEmptyForm(row?.role || "ADMIN"),
      role: row?.role || "ADMIN",
      email: row?.email || "",
      password: "",
      firstName: split.firstName,
      lastName: split.lastName,
      name: row?.name || "",
      phone: row?.phone || "",
      countryCode: row?.countryCode || "+91",
      employeeCode: row?.employeeCode || "",
      departmentText: row?.department || "",
      qualification: row?.qualification || "",
      specialization: org?.specialization || row?.department || "",
      experienceYears: org?.experienceYears ?? "",
      certification: org?.certification || "",
      ratePerHour: org?.ratePerHour ?? "",
      weight: org?.weight ?? "",
      height: org?.height ?? "",
      bloodGroup: org?.bloodGroup || "",
      age: org?.age ?? "",
      gender: org?.gender || "",
      address: org?.address || "",
      city: org?.city || "",
      medicalConditions: org?.medicalConditions || "",
      emergencyContact: org?.emergencyContact || "",
      emergencyPhone: org?.emergencyPhone || "",
      status: String(row?.status || "ACTIVE").toUpperCase(),
      headOfficeId: org?.headOfficeId ? String(org.headOfficeId) : "",
      branchId: org?.branchId ? String(org.branchId) : "",
      departmentId: org?.departmentId ? String(org.departmentId) : "",
      teamId: org?.teamId ? String(org.teamId) : "",
      designationId: org?.designationId ? String(org.designationId) : "",
      joinDate: toDateInputValue(org?.joinDate),
      bio: org?.bio || "",
      dateOfBirth: toDateInputValue(org?.dateOfBirth),
      personalEmail: org?.personalEmail || "",
      alternatePhone: org?.alternatePhone || "",
      currentAddress: org?.currentAddress || "",
      permanentAddress: org?.permanentAddress || "",
      state: org?.state || "",
      pincode: org?.pincode || "",
      employmentType: org?.employmentType || "",
      workLocation: org?.workLocation || "",
      reportingManagerName: org?.reportingManagerName || org?.reportsToName || "",
      reportsToId: org?.reportsToId ? String(org.reportsToId) : "",
      fatherName: org?.fatherName || "",
      motherName: org?.motherName || "",
      maritalStatus: org?.maritalStatus || "",
      spouseName: org?.spouseName || "",
      location: org?.location || "",
      probationEndDate: toDateInputValue(org?.probationEndDate),
      panNumber: org?.panNumber || "",
      aadharNumber: org?.aadharNumber || "",
      bankName: org?.bankName || "",
      bankAccountNumber: org?.bankAccountNumber || "",
      bankIfscCode: org?.bankIfscCode || "",
      bankAccountType: org?.bankAccountType || "",
      bankAccountHolderName: org?.bankAccountHolderName || "",
      bankBranch: org?.bankBranch || "",
      qualificationDocumentPath: org?.qualificationDocumentPath || "",
      certificationDocumentPath: org?.certificationDocumentPath || "",
      idProofDocumentPath: org?.idProofDocumentPath || "",
      addressProofDocumentPath: org?.addressProofDocumentPath || "",
      resumeDocumentPath: org?.resumeDocumentPath || "",
      offerLetterDocumentPath: org?.offerLetterDocumentPath || "",
      candidatePhotoPath: org?.candidatePhotoPath || "",
      aadharCardDocumentPath: org?.aadharCardDocumentPath || "",
      panCardDocumentPath: org?.panCardDocumentPath || "",
      bankDocumentPath: org?.bankDocumentPath || "",
      previousEmployment1: org?.previousEmployment1 || "",
      previousEmployment2: org?.previousEmployment2 || "",
      experienceCertificateDocumentPath: org?.experienceCertificateDocumentPath || "",
      courseCertificatePath: org?.courseCertificatePath || "",
      educationCertificatePath: org?.educationCertificatePath || "",
      emergencyContactRelationship: org?.emergencyContactRelationship || "",
      emergencyContactName2: org?.emergencyContactName2 || "",
      emergencyContactRelationship2: org?.emergencyContactRelationship2 || "",
      emergencyPhone2: org?.emergencyPhone2 || "",
      referenceName1: org?.referenceName1 || "",
      referencePhone1: org?.referencePhone1 || "",
      referenceName2: org?.referenceName2 || "",
      referencePhone2: org?.referencePhone2 || "",
      joiningBranchName: org?.joiningBranchName || "",
      sourcePlatform: org?.sourcePlatform || "",
      pfUan: org?.pfUan || "",
      esiNumber: org?.esiNumber || "",
      declarationDate: toDateInputValue(org?.declarationDate),
      declarationPlace: org?.declarationPlace || "",
    });

    setSelectedRow(row);
    setIsEdit(true);
    setModalError("");
    setModalTab("identity");
    setCountrySearch("");
    setCountryDropdownOpen(false);
    setShowModal(true);
  };

  const buildSharedPayload = () => {
    const normalizedCountryCode = ensureCountryCodeValue(form.countryCode);
    const option = getCountryOptionByValue(normalizedCountryCode);
    const lengths = getCountryAllowedLengths(normalizedCountryCode);
    const digits = sanitizePhoneDigits(form.phone, option?.maxLength, lengths);
    const fullPhone = digits ? `${normalizedCountryCode}${digits}` : "";
    const split = splitName(form.name || [form.firstName, form.lastName].filter(Boolean).join(" "));

    return {
      email: form.email.trim(),
      password: form.password.trim(),
      firstName: split.firstName.trim(),
      lastName: split.lastName.trim(),
      name: [split.firstName.trim(), split.lastName.trim()].filter(Boolean).join(" "),
      phone: fullPhone,
      headOfficeId: toSafeId(form.headOfficeId),
      branchId: toSafeId(form.branchId),
      departmentId: toSafeId(form.departmentId),
      teamId: toSafeId(form.teamId),
      designationId: toSafeId(form.designationId),
      departmentName: selectedDepartment?.name || form.departmentText.trim() || "",
      orgPreview,
    };
  };

  const buildEmployeeExtras = () => {
    const text = (value) => (typeof value === "string" ? value.trim() : "");

    return {
      dateOfBirth: form.dateOfBirth || null,
      gender: text(form.gender) || null,
      bloodGroup: text(form.bloodGroup) || null,
      personalEmail: text(form.personalEmail) || null,
      alternatePhone: text(form.alternatePhone) || null,
      emergencyContact: text(form.emergencyContact) || null,
      emergencyPhone: text(form.emergencyPhone) || null,
      currentAddress: text(form.currentAddress) || null,
      permanentAddress: text(form.permanentAddress) || null,
      city: text(form.city) || null,
      state: text(form.state) || null,
      pincode: text(form.pincode) || null,
      employmentType: text(form.employmentType) || null,
      workLocation: text(form.workLocation) || null,
      reportingManagerName: reportingOptions.find((item) => String(item.id) === String(form.reportsToId))?.name || null,
      reportsToId: toSafeId(form.reportsToId),
      fatherName: text(form.fatherName) || null,
      motherName: text(form.motherName) || null,
      maritalStatus: text(form.maritalStatus) || null,
      spouseName: text(form.spouseName) || null,
      location: text(form.location) || null,
      probationEndDate: form.probationEndDate || null,
      panNumber: text(form.panNumber) || null,
      aadharNumber: text(form.aadharNumber) || null,
      bankName: text(form.bankName) || null,
      bankAccountNumber: text(form.bankAccountNumber) || null,
      bankIfscCode: text(form.bankIfscCode) || null,
      bankAccountType: text(form.bankAccountType) || null,
      bankAccountHolderName: text(form.bankAccountHolderName) || null,
      bankBranch: text(form.bankBranch) || null,
      qualificationDocumentPath: text(form.qualificationDocumentPath) || null,
      certificationDocumentPath: text(form.certificationDocumentPath) || null,
      idProofDocumentPath: text(form.idProofDocumentPath) || null,
      addressProofDocumentPath: text(form.addressProofDocumentPath) || null,
      resumeDocumentPath: text(form.resumeDocumentPath) || null,
      offerLetterDocumentPath: text(form.offerLetterDocumentPath) || null,
      candidatePhotoPath: text(form.candidatePhotoPath) || null,
      aadharCardDocumentPath: text(form.aadharCardDocumentPath) || null,
      panCardDocumentPath: text(form.panCardDocumentPath) || null,
      bankDocumentPath: text(form.bankDocumentPath) || null,
      previousEmployment1: text(form.previousEmployment1) || null,
      previousEmployment2: text(form.previousEmployment2) || null,
      experienceCertificateDocumentPath: text(form.experienceCertificateDocumentPath) || null,
      courseCertificatePath: text(form.courseCertificatePath) || null,
      educationCertificatePath: text(form.educationCertificatePath) || null,
      emergencyContactRelationship: text(form.emergencyContactRelationship) || null,
      emergencyContactName2: text(form.emergencyContactName2) || null,
      emergencyContactRelationship2: text(form.emergencyContactRelationship2) || null,
      emergencyPhone2: text(form.emergencyPhone2) || null,
      referenceName1: text(form.referenceName1) || null,
      referencePhone1: text(form.referencePhone1) || null,
      referenceName2: text(form.referenceName2) || null,
      referencePhone2: text(form.referencePhone2) || null,
      joiningBranchName: text(form.joiningBranchName) || null,
      sourcePlatform: text(form.sourcePlatform) || null,
      pfUan: text(form.pfUan) || null,
      esiNumber: text(form.esiNumber) || null,
      declarationDate: form.declarationDate || null,
      declarationPlace: text(form.declarationPlace) || null,
    };
  };

  const buildCreateRequest = () => {
    const role = normalizeRole(form.role);
    const shared = buildSharedPayload();

    if (role === "SUPER_ADMIN") {
      return {
        endpoint: "/users/super-admin",
        params: { creatorId: currentUserId },
        payload: {
          email: shared.email,
          password: shared.password,
          firstName: shared.firstName,
          lastName: shared.lastName,
        },
      };
    }

    if (role === "ADMIN") {
      return {
        endpoint: "/users/admin",
        params: { creatorId: currentUserId },
        payload: {
          email: shared.email,
          password: shared.password,
          firstName: shared.firstName,
          lastName: shared.lastName,
          department: shared.departmentName,
          phone: shared.phone,
          employeeId: form.employeeCode.trim() || `EMP${Date.now()}`,
          qualification: form.qualification.trim() || "N/A",
          joinDate: form.joinDate || null,
          headOfficeId: shared.headOfficeId,
          branchId: shared.branchId,
          departmentId: shared.departmentId,
          teamId: shared.teamId,
          designationId: shared.designationId,
          ...buildEmployeeExtras(),
        },
      };
    }

    if (role === "MANAGER") {
      return {
        endpoint: "/users/manager",
        params: { creatorId: currentUserId },
        payload: {
          email: shared.email,
          password: shared.password,
          firstName: shared.firstName,
          lastName: shared.lastName,
          department: shared.departmentName,
          phone: shared.phone,
          employeeId: form.employeeCode.trim() || `EMP${Date.now()}`,
          qualification: form.qualification.trim() || "N/A",
          joinDate: form.joinDate || null,
          headOfficeId: shared.headOfficeId,
          branchId: shared.branchId,
          departmentId: shared.departmentId,
          teamId: shared.teamId,
          designationId: shared.designationId,
          ...buildEmployeeExtras(),
        },
      };
    }

    if (role === "TRAINER") {
      return {
        endpoint: "/users/trainer",
        params: { creatorId: currentUserId },
        payload: {
          email: shared.email,
          password: shared.password,
          firstName: shared.firstName,
          lastName: shared.lastName,
          specialization: form.specialization.trim() || shared.departmentName || "General Training",
          experienceYears: Number(form.experienceYears) || 0,
          certification: form.certification.trim() || "N/A",
          phone: shared.phone,
          qualification: form.qualification.trim() || "N/A",
          ratePerHour: Number(form.ratePerHour) || 0,
          languages: form.languages.trim() || null,
          rating: form.rating === "" ? null : Number(form.rating),
          totalClientsTrained: form.totalClientsTrained === "" ? null : Number(form.totalClientsTrained),
          joinDate: form.joinDate || null,
          headOfficeId: shared.headOfficeId,
          branchId: shared.branchId,
          departmentId: shared.departmentId,
          teamId: shared.teamId,
          designationId: shared.designationId,
          ...buildEmployeeExtras(),
        },
      };
    }

    if (role === "USER") {
      return {
        endpoint: "/users/customer/by-trainer",
        params: { trainerId: currentUserId },
        payload: {
          email: shared.email,
          password: shared.password,
          firstName: shared.firstName,
          lastName: shared.lastName,
          weight: Number(form.weight) || 0,
          height: Number(form.height) || 0,
          bloodGroup: form.bloodGroup || "O+",
          age: Number(form.age) || 0,
          gender: form.gender || "Prefer not to say",
          phone: shared.phone,
          address: form.address.trim() || "",
          city: form.city.trim() || "",
          medicalConditions: form.medicalConditions.trim() || null,
          emergencyContact: form.emergencyContact.trim() || "",
          emergencyPhone: form.emergencyPhone.trim() || "",
          headOfficeId: shared.headOfficeId,
          branchId: shared.branchId,
          departmentId: shared.departmentId,
          teamId: shared.teamId,
          designationId: shared.designationId,
        },
      };
    }

    throw new Error(
      `${role} creation is not available in the current backend. Add the matching user endpoint before enabling this role.`,
    );
  };

  const buildUpdateRequest = () => {
    const role = normalizeRole(selectedRow?.role);
    const shared = buildSharedPayload();

    if (role === "SUPER_ADMIN") {
      return {
        endpoint: `/users/super-admin/${selectedRow.id}`,
        params: { updaterId: currentUserId },
        payload: {
          email: shared.email,
          password: form.password.trim() || "Temp@123",
          firstName: shared.firstName,
          lastName: shared.lastName,
        },
      };
    }

    if (role === "ADMIN") {
      return {
        endpoint: `/users/admin/${selectedRow.id}`,
        params: { updaterId: currentUserId },
        payload: {
          email: shared.email,
          password: form.password.trim() || "Temp@123",
          firstName: shared.firstName,
          lastName: shared.lastName,
          department: shared.departmentName,
          phone: shared.phone,
          employeeId: form.employeeCode.trim() || "",
          qualification: form.qualification.trim() || "N/A",
          joinDate: form.joinDate || null,
          headOfficeId: shared.headOfficeId,
          branchId: shared.branchId,
          departmentId: shared.departmentId,
          teamId: shared.teamId,
          designationId: shared.designationId,
          ...buildEmployeeExtras(),
        },
      };
    }

    if (role === "MANAGER") {
      return {
        endpoint: `/users/manager/${selectedRow.id}`,
        params: { updaterId: currentUserId },
        payload: {
          email: shared.email,
          password: form.password.trim() || "Temp@123",
          firstName: shared.firstName,
          lastName: shared.lastName,
          department: shared.departmentName,
          phone: shared.phone,
          employeeId: form.employeeCode.trim() || "",
          qualification: form.qualification.trim() || "N/A",
          joinDate: form.joinDate || null,
          headOfficeId: shared.headOfficeId,
          branchId: shared.branchId,
          departmentId: shared.departmentId,
          teamId: shared.teamId,
          designationId: shared.designationId,
          ...buildEmployeeExtras(),
        },
      };
    }

    if (role === "TRAINER") {
      return {
        endpoint: `/users/trainer/${selectedRow.id}`,
        params: { updaterId: currentUserId },
        payload: {
          email: shared.email,
          password: form.password.trim() || "Temp@123",
          firstName: shared.firstName,
          lastName: shared.lastName,
          specialization: form.specialization.trim() || shared.departmentName || "General Training",
          experienceYears: Number(form.experienceYears) || 0,
          certification: form.certification.trim() || "N/A",
          phone: shared.phone,
          qualification: form.qualification.trim() || "N/A",
          ratePerHour: Number(form.ratePerHour) || 0,
          languages: form.languages.trim() || null,
          rating: form.rating === "" ? null : Number(form.rating),
          totalClientsTrained: form.totalClientsTrained === "" ? null : Number(form.totalClientsTrained),
          joinDate: form.joinDate || null,
          headOfficeId: shared.headOfficeId,
          branchId: shared.branchId,
          departmentId: shared.departmentId,
          teamId: shared.teamId,
          designationId: shared.designationId,
          ...buildEmployeeExtras(),
        },
      };
    }

    if (role === "USER") {
      return {
        endpoint: `/users/customer/${selectedRow.id}`,
        params: { updaterId: currentUserId },
        payload: {
          weight: Number(form.weight) || 0,
          height: Number(form.height) || 0,
          bloodGroup: form.bloodGroup || "O+",
          age: Number(form.age) || 0,
          gender: form.gender || "Prefer not to say",
          phone: shared.phone,
          address: form.address.trim() || "",
          city: form.city.trim() || "",
          medicalConditions: form.medicalConditions.trim() || null,
          emergencyContact: form.emergencyContact.trim() || "",
          emergencyPhone: form.emergencyPhone.trim() || "",
        },
      };
    }

    throw new Error(`${role} records are not editable from this page until the backend exposes that route.`);
  };

  const validateForm = () => {
    const role = normalizeRole(isEdit ? selectedRow?.role : form.role);

    if (!role) return "Please select a role";
    if (!form.email.trim()) return "Email is required";
    if (!isEdit && !form.password.trim()) return "Password is required";
    if (!isEdit && role === "SUPER_ADMIN" && form.password.trim().length < 8) {
      return "Super Admin password must be at least 8 characters";
    }
    if (
      !isEdit &&
      ["ADMIN", "MANAGER", "TRAINER"].includes(role) &&
      !STAFF_PASSWORD_PATTERN.test(form.password.trim())
    ) {
      return "Password must contain uppercase, lowercase, digit, and special character";
    }
    if (isEdit && role === "SUPER_ADMIN" && !form.password.trim()) {
      return "Password is required when updating a Super Admin";
    }
    if (isEdit && role === "SUPER_ADMIN" && form.password.trim().length < 8) {
      return "Super Admin password must be at least 8 characters";
    }
    if (
      isEdit &&
      ["ADMIN", "MANAGER", "TRAINER"].includes(role) &&
      form.password.trim() &&
      !STAFF_PASSWORD_PATTERN.test(form.password.trim())
    ) {
      return "Password must contain uppercase, lowercase, digit, and special character";
    }
    if (!form.name.trim()) return "Name is required";
    if (form.name.trim().length < 2) return "Name must be at least 2 characters";

    if (form.phone) {
      const phoneValidation = validatePhoneNumber(form.phone, form.countryCode);
      if (phoneValidation) return phoneValidation;
    }

    if (!isEdit && !currentUserId) return "Current user is not available for this action";

    if (role === "ADMIN" || role === "MANAGER") {
      const label = role === "ADMIN" ? "Admin" : "Manager";
      if (!form.employeeCode.trim()) return `Employee code is required for ${label}`;
      if (!form.qualification.trim()) return `Qualification is required for ${label}`;
      if (!form.departmentId && !form.departmentText.trim()) return `Department is required for ${label}`;
    }

    if (role === "TRAINER") {
      if (!form.specialization.trim()) return "Specialization is required for Trainer";
      if (!String(form.experienceYears).trim()) return "Experience years is required for Trainer";
      if (!form.certification.trim()) return "Certification is required for Trainer";
      if (!form.qualification.trim()) return "Qualification is required for Trainer";
      if (!String(form.ratePerHour).trim()) return "Rate per hour is required for Trainer";
    }

    if (role === "USER") {
      if (!String(form.weight).trim()) return "Weight is required for User";
      if (!String(form.height).trim()) return "Height is required for User";
      if (!form.bloodGroup.trim()) return "Blood group is required for User";
      if (!String(form.age).trim()) return "Age is required for User";
      if (!form.gender.trim()) return "Gender is required for User";
      if (!form.address.trim()) return "Address is required for User";
      if (!form.city.trim()) return "City is required for User";
      if (!form.emergencyContact.trim()) return "Emergency contact is required for User";
      if (!form.emergencyPhone.trim()) return "Emergency phone is required for User";
    }

    if (!form.headOfficeId && !form.branchId && !form.departmentId && !form.teamId && !form.designationId) {
      return null;
    }

    if (form.branchId && !form.headOfficeId) return "Select a head office before choosing a branch";
    if (form.departmentId && !form.branchId) return "Select a branch before choosing a department";
    if (form.teamId && !form.departmentId) return "Select a department before choosing a team";
    if (form.designationId && !form.departmentId) return "Select a department before choosing a designation";

    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setModalError("");

    const validationMessage = validateForm();
    if (validationMessage) {
      setModalError(validationMessage);
      setModalTab(validationMessage.toLowerCase().includes("office") || validationMessage.toLowerCase().includes("branch")
        || validationMessage.toLowerCase().includes("department") || validationMessage.toLowerCase().includes("team")
        || validationMessage.toLowerCase().includes("designation")
        ? "organization"
        : "identity");
      return;
    }

    const targetRole = normalizeRole(isEdit ? selectedRow?.role : form.role);
    if (!isEdit && !createAvailability[targetRole]) {
      setModalError(
        `${targetRole} creation is currently not enabled for your logged-in role. The backend exposes only the supported create routes.`,
      );
      setModalTab("identity");
      return;
    }

    setSaving(true);
    try {
      const request = isEdit ? buildUpdateRequest() : buildCreateRequest();

      await api.request({
        method: isEdit ? "put" : "post",
        url: request.endpoint,
        params: request.params,
        data: request.payload,
      });

      setNotice(isEdit ? "Employee updated successfully" : "Employee added successfully");
      setShowModal(false);
      setSelectedRow(null);
      setForm(createEmptyForm(getDefaultCreateRole(currentRole)));
      await loadRows();
    } catch (e) {
      setModalError(extractApiErrorMessage(e, "Operation failed"));
    } finally {
      setSaving(false);
    }
  };

  const confirmDelete = (row) => {
    setDeleteTarget(row);
  };

  const handleDelete = async () => {
    if (!deleteTarget?.id) return;

    setSaving(true);
    setError("");

    try {
      const role = normalizeRole(deleteTarget.role);
      let request;

      if (role === "SUPER_ADMIN") {
        request = { method: "delete", url: `/users/super-admin/${deleteTarget.id}`, params: { deleterId: currentUserId } };
      } else if (role === "ADMIN") {
        request = { method: "delete", url: `/users/admin/${deleteTarget.id}`, params: { deleterId: currentUserId } };
      } else if (role === "MANAGER") {
        request = { method: "delete", url: `/users/manager/${deleteTarget.id}`, params: { deleterId: currentUserId } };
      } else if (role === "TRAINER") {
        request = { method: "delete", url: `/users/trainer/${deleteTarget.id}`, params: { deleterId: currentUserId } };
      } else if (role === "USER") {
        request = { method: "delete", url: `/users/customer/${deleteTarget.id}`, params: { deleterId: currentUserId } };
      } else {
        throw new Error(`${role} records cannot be deleted from this page until the backend exposes that route.`);
      }

      await api.request(request);
      setNotice("Employee deleted successfully");
      setDeleteTarget(null);
      await loadRows();
    } catch (e) {
      setError(extractApiErrorMessage(e, "Failed to delete employee"));
    } finally {
      setSaving(false);
    }
  };

  const handleRemoveInactiveUsers = async () => {
    if (inactiveRows.length === 0) return;

    const confirmed = window.confirm(
      `Remove ${inactiveRows.length} inactive record(s)? This cannot be undone.`,
    );
    if (!confirmed) return;

    setSaving(true);
    setError("");

    try {
      const results = await Promise.allSettled(
        inactiveRows.map((row) => {
          if (row.role === "SUPER_ADMIN") {
            return api.delete(`/users/super-admin/${row.id}`, { params: { deleterId: currentUserId } });
          }
          if (row.role === "ADMIN") {
            return api.delete(`/users/admin/${row.id}`, { params: { deleterId: currentUserId } });
          }
          if (row.role === "MANAGER") {
            return api.delete(`/users/manager/${row.id}`, { params: { deleterId: currentUserId } });
          }
          if (row.role === "TRAINER") {
            return api.delete(`/users/trainer/${row.id}`, { params: { deleterId: currentUserId } });
          }
          if (row.role === "USER") {
            return api.delete(`/users/customer/${row.id}`, { params: { deleterId: currentUserId } });
          }
          throw new Error(`${row.role} records are not supported yet.`);
        }),
      );

      const successCount = results.filter((item) => item.status === "fulfilled").length;
      const failedCount = results.length - successCount;

      if (failedCount > 0) {
        setError(`${failedCount} inactive record(s) could not be removed`);
      }

      if (successCount > 0) {
        setNotice(`${successCount} inactive record(s) removed`);
      }

      await loadRows();
    } catch (e) {
      setError(extractApiErrorMessage(e, "Failed to remove inactive records"));
    } finally {
      setSaving(false);
    }
  };

  const countryOptions = useMemo(() => getCountryOptions(countrySearch), [countrySearch]);

  const roleSelectOptions = useMemo(() => {
    const availability = createAvailability;
    return ROLE_OPTIONS.filter((item) => (viewMode === "users" ? item.value === "USER" : item.value !== "USER")).map((item) => ({
      ...item,
      disabled: !availability[item.value],
    }));
  }, [createAvailability, viewMode]);

  const renderStats = () => (
    <div className="row mb-4">
      <StatCard value={displayedRows.length} label={viewMode === "users" ? "Total Users" : "Total Employees"} />
      <StatCard value={activeRows.length} label="Active Records" />
      <StatCard value={inactiveRows.length} label="Inactive Records" />
      <StatCard value={totalRoles} label="Role Types" />
    </div>
  );

  const renderDisplaySwitch = () => (
    <div className="d-flex flex-wrap gap-2 mb-3">
      <Button
        type="button"
        variant={viewMode === "employees" ? "primary" : "light"}
        onClick={() => setViewMode("employees")}
      >
        Employees ({employeeRows.length})
      </Button>
      <Button
        type="button"
        variant={viewMode === "users" ? "primary" : "light"}
        onClick={() => setViewMode("users")}
      >
        Users ({userRows.length})
      </Button>
    </div>
  );

  const renderHeader = (title, activeLabel) => (
    <div className="d-md-flex d-block align-items-center justify-content-between page-breadcrumb mb-3">
      <div className="my-auto mb-2">
        <h2 className="mb-1">{title}</h2>
        <nav>
          <ol className="breadcrumb mb-0">
            <li className="breadcrumb-item">
              <Link to="/">
                <IconHome size={16} />
              </Link>
            </li>
            <li className="breadcrumb-item">Settings</li>
            <li className="breadcrumb-item active">{activeLabel}</li>
          </ol>
        </nav>
      </div>
      <div className="d-flex gap-2 flex-wrap">
        <button
          className="btn btn-outline-danger"
          onClick={handleRemoveInactiveUsers}
          disabled={saving || inactiveRows.length === 0}
        >
          Remove Inactive Records
        </button>
        <button className="btn btn-primary" onClick={openAdd} disabled={saving || !canCreateInCurrentView}>
          <IconPlus size={16} className="me-2" />
          {viewMode === "users" ? "Add User" : "Add Employee"}
        </button>
      </div>
    </div>
  );

  const renderTabs = () => (
    <div className="d-flex gap-2 mb-3 flex-wrap">
      {[
        ["identity", "Identity"],
        ["organization", "Organization"],
        ["details", "Role Details"],
        ["personal", "Personal Details"],
        ["documents", "Documents"],
      ].map(([key, label]) => (
        <Button
          key={key}
          type="button"
          variant={modalTab === key ? "primary" : "light"}
          onClick={() => setModalTab(key)}
        >
          {label}
        </Button>
      ))}
    </div>
  );

  const renderRoleSpecificFields = () => {
    const role = normalizeRole(isEdit ? selectedRow?.role : form.role);

    if (role === "ADMIN" || role === "MANAGER") {
      const label = role === "ADMIN" ? "Admin Details" : "Manager Details";
      return (
        <div className="row g-3">
          <SectionHeader label={label} />
          <div className="col-md-6">
            <label className="form-label">Employee Code *</label>
            <input
              className="form-control"
              value={form.employeeCode}
              onChange={(e) => setForm({ ...form, employeeCode: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Qualification *</label>
            <select
              className="form-select"
              value={form.qualification}
              onChange={(e) => setForm({ ...form, qualification: e.target.value })}
            >
              <option value="">Select qualification</option>
              {QUALIFICATION_OPTIONS.map((option) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
          </div>
          <div className="col-md-6">
            <label className="form-label">Department Label</label>
            <input
              className="form-control"
              value={form.departmentText}
              onChange={(e) => setForm({ ...form, departmentText: e.target.value })}
              placeholder="Usually derived from the selected department master"
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Join Date</label>
            <input
              type="date"
              className="form-control"
              value={form.joinDate}
              onChange={(e) => setForm({ ...form, joinDate: e.target.value })}
            />
          </div>
        </div>
      );
    }

    if (role === "TRAINER") {
      return (
        <div className="row g-3">
          <SectionHeader label="Trainer Details" />
          <div className="col-md-6">
            <label className="form-label">Specialization *</label>
            <input
              className="form-control"
              value={form.specialization}
              onChange={(e) => setForm({ ...form, specialization: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Experience Years *</label>
            <input
              type="number"
              min="0"
              className="form-control"
              value={form.experienceYears}
              onChange={(e) => setForm({ ...form, experienceYears: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Certification *</label>
            <input
              className="form-control"
              value={form.certification}
              onChange={(e) => setForm({ ...form, certification: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Qualification *</label>
            <select
              className="form-select"
              value={form.qualification}
              onChange={(e) => setForm({ ...form, qualification: e.target.value })}
            >
              <option value="">Select qualification</option>
              {QUALIFICATION_OPTIONS.map((option) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
          </div>
          <div className="col-md-6">
            <label className="form-label">Rate Per Hour *</label>
            <input
              type="number"
              min="0"
              step="0.01"
              className="form-control"
              value={form.ratePerHour}
              onChange={(e) => setForm({ ...form, ratePerHour: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Join Date</label>
            <input
              type="date"
              className="form-control"
              value={form.joinDate}
              onChange={(e) => setForm({ ...form, joinDate: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Languages</label>
            <input
              className="form-control"
              value={form.languages}
              onChange={(e) => setForm({ ...form, languages: e.target.value })}
              placeholder="English, Hindi"
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Rating</label>
            <input
              type="number"
              min="0"
              max="5"
              step="0.01"
              className="form-control"
              value={form.rating}
              onChange={(e) => setForm({ ...form, rating: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Clients Trained</label>
            <input
              type="number"
              min="0"
              className="form-control"
              value={form.totalClientsTrained}
              onChange={(e) => setForm({ ...form, totalClientsTrained: e.target.value })}
            />
          </div>
        </div>
      );
    }

    if (role === "USER") {
      return (
        <div className="row g-3">
          <SectionHeader label="User / Customer Details" />
          <div className="col-md-6">
            <label className="form-label">Weight (kg) *</label>
            <input
              type="number"
              min="0"
              step="0.1"
              className="form-control"
              value={form.weight}
              onChange={(e) => setForm({ ...form, weight: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Height (cm) *</label>
            <input
              type="number"
              min="0"
              step="0.1"
              className="form-control"
              value={form.height}
              onChange={(e) => setForm({ ...form, height: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Blood Group *</label>
            <select
              className="form-select"
              value={form.bloodGroup}
              onChange={(e) => setForm({ ...form, bloodGroup: e.target.value })}
            >
              <option value="">Select</option>
              {BLOOD_GROUP_OPTIONS.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </div>
          <div className="col-md-6">
            <label className="form-label">Age *</label>
            <input
              type="number"
              min="0"
              className="form-control"
              value={form.age}
              onChange={(e) => setForm({ ...form, age: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Gender *</label>
            <select
              className="form-select"
              value={form.gender}
              onChange={(e) => setForm({ ...form, gender: e.target.value })}
            >
              <option value="">Select</option>
              {GENDER_OPTIONS.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </div>
          <div className="col-md-6">
            <label className="form-label">Address *</label>
            <input
              className="form-control"
              value={form.address}
              onChange={(e) => setForm({ ...form, address: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">City *</label>
            <input
              className="form-control"
              value={form.city}
              onChange={(e) => setForm({ ...form, city: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Medical Conditions</label>
            <input
              className="form-control"
              value={form.medicalConditions}
              onChange={(e) => setForm({ ...form, medicalConditions: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Emergency Contact *</label>
            <input
              className="form-control"
              value={form.emergencyContact}
              onChange={(e) => setForm({ ...form, emergencyContact: e.target.value })}
            />
          </div>
          <div className="col-md-6">
            <label className="form-label">Emergency Phone *</label>
            <input
              className="form-control"
              value={form.emergencyPhone}
              onChange={(e) => setForm({ ...form, emergencyPhone: e.target.value.replace(/\D/g, "") })}
            />
          </div>
        </div>
      );
    }

    if (role === "SUPER_ADMIN") {
      return (
        <div className="alert alert-light mb-0">
          Super Admin uses only the identity fields for this form.
        </div>
      );
    }

    return <div className="alert alert-warning mb-0">This role is not supported by user management yet.</div>;
  };


  const renderEmployeePersonalFields = () => {
    const role = normalizeRole(isEdit ? selectedRow?.role : form.role);

    if (!["ADMIN", "MANAGER", "TRAINER"].includes(role)) {
      return <div className="alert alert-light mb-0">Personal details are used for employees only.</div>;
    }

    return (
      <div className="row g-3">
        <SectionHeader label="Personal Details" />
        <div className="col-md-6">
          <label className="form-label">Date of Birth</label>
          <input type="date" className="form-control" value={form.dateOfBirth} onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })} />
        </div>
        <div className="col-md-6"><label className="form-label">Father's Name</label><input className="form-control" value={form.fatherName} onChange={(e) => setForm({ ...form, fatherName: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Mother's Name</label><input className="form-control" value={form.motherName} onChange={(e) => setForm({ ...form, motherName: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Marital Status</label><select className="form-select" value={form.maritalStatus} onChange={(e) => setForm({ ...form, maritalStatus: e.target.value })}><option value="">Select</option>{MARITAL_STATUS_OPTIONS.map((item) => <option key={item} value={item}>{item}</option>)}</select></div>
        <div className="col-md-6"><label className="form-label">Spouse Name</label><input className="form-control" value={form.spouseName} disabled={form.maritalStatus !== "Married"} onChange={(e) => setForm({ ...form, spouseName: e.target.value })} /></div>        <div className="col-md-6">
          <label className="form-label">Gender</label>
          <select className="form-select" value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value })}>
            <option value="">Select</option>
            {GENDER_OPTIONS.map((item) => <option key={item} value={item}>{item}</option>)}
          </select>
        </div>
        <div className="col-md-6">
          <label className="form-label">Blood Group</label>
          <select className="form-select" value={form.bloodGroup} onChange={(e) => setForm({ ...form, bloodGroup: e.target.value })}>
            <option value="">Select</option>
            {BLOOD_GROUP_OPTIONS.map((item) => <option key={item} value={item}>{item}</option>)}
          </select>
        </div>
        <div className="col-md-6">
          <label className="form-label">Personal Email</label>
          <input type="email" className="form-control" value={form.personalEmail} onChange={(e) => setForm({ ...form, personalEmail: e.target.value })} />
        </div>
        <div className="col-md-6">
          <label className="form-label">Alternate Phone</label>
          <input className="form-control" value={form.alternatePhone} onChange={(e) => setForm({ ...form, alternatePhone: e.target.value.replace(/[^+\d]/g, "") })} />
        </div>
        <SectionHeader label="Emergency" />
        <div className="col-md-6"><label className="form-label">Emergency Name 1</label><input className="form-control" value={form.emergencyContact} onChange={(e) => setForm({ ...form, emergencyContact: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Emergency Contact 1</label><input className="form-control" value={form.emergencyPhone} onChange={(e) => setForm({ ...form, emergencyPhone: e.target.value.replace(/[^+\d]/g, "") })} /></div>
        <div className="col-md-6"><label className="form-label">Emergency Relationship 1</label><input className="form-control" value={form.emergencyContactRelationship} onChange={(e) => setForm({ ...form, emergencyContactRelationship: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Emergency Name 2</label><input className="form-control" value={form.emergencyContactName2} onChange={(e) => setForm({ ...form, emergencyContactName2: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Emergency Contact 2</label><input className="form-control" value={form.emergencyPhone2} onChange={(e) => setForm({ ...form, emergencyPhone2: e.target.value.replace(/[^+\d]/g, "") })} /></div>
        <div className="col-md-6"><label className="form-label">Emergency Relationship 2</label><input className="form-control" value={form.emergencyContactRelationship2} onChange={(e) => setForm({ ...form, emergencyContactRelationship2: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Location</label><input className="form-control" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} /></div>
        <div className="col-md-6">
          <label className="form-label">City</label>
          <input className="form-control" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} />
        </div>
        <div className="col-md-6">
          <label className="form-label">State</label>
          <input className="form-control" value={form.state} onChange={(e) => setForm({ ...form, state: e.target.value })} />
        </div>
        <div className="col-md-6">
          <label className="form-label">Pincode</label>
          <input className="form-control" value={form.pincode} onChange={(e) => setForm({ ...form, pincode: e.target.value })} />
        </div>
        <div className="col-md-12">
          <label className="form-label">Current Address</label>
          <textarea className="form-control" rows={2} value={form.currentAddress} onChange={(e) => setForm({ ...form, currentAddress: e.target.value })} />
        </div>
        <div className="col-md-12">
          <label className="form-label">Permanent Address</label>
          <textarea className="form-control" rows={2} value={form.permanentAddress} onChange={(e) => setForm({ ...form, permanentAddress: e.target.value })} />
        </div>
        <SectionHeader label="Employment" />
        <div className="col-md-6">
          <label className="form-label">Employment Type</label>
          <select className="form-select" value={form.employmentType} onChange={(e) => setForm({ ...form, employmentType: e.target.value })}>
            <option value="">Select</option>
            {EMPLOYMENT_TYPE_OPTIONS.map((item) => <option key={item} value={item}>{item}</option>)}
          </select>
        </div>
        <div className="col-md-6">
          <label className="form-label">Work Location</label>
          <select className="form-select" value={form.workLocation} onChange={(e) => setForm({ ...form, workLocation: e.target.value })}>
            <option value="">Select</option>
            {WORK_LOCATION_OPTIONS.map((item) => <option key={item} value={item}>{item}</option>)}
          </select>
        </div>
        <div className="col-md-6">
          <label className="form-label">Reports To</label>
          <select
            className="form-select"
            value={form.reportsToId}
            disabled={reportingOptionsLoading}
            onChange={(e) => setForm({ ...form, reportsToId: e.target.value })}
          >
            <option value="">No reporting user</option>
            {reportingOptions.map((item) => (
              <option key={item.id} value={item.id}>
                {item.name} ({item.role})
              </option>
            ))}
          </select>
        </div>
        <div className="col-md-6"><label className="form-label">Joining Branch Name</label><input className="form-control" value={form.joiningBranchName} onChange={(e) => setForm({ ...form, joiningBranchName: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Source Platform</label><select className="form-select" value={form.sourcePlatform} onChange={(e) => setForm({ ...form, sourcePlatform: e.target.value })}><option value="">Select</option>{SOURCE_PLATFORM_OPTIONS.map((item) => <option key={item} value={item}>{item}</option>)}</select></div>
        <div className="col-md-6"><label className="form-label">PF UAN</label><input className="form-control" value={form.pfUan} onChange={(e) => setForm({ ...form, pfUan: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">ESI Number</label><input className="form-control" value={form.esiNumber} onChange={(e) => setForm({ ...form, esiNumber: e.target.value })} /></div>        <div className="col-md-6">
          <label className="form-label">Probation End Date</label>
          <input type="date" className="form-control" value={form.probationEndDate} onChange={(e) => setForm({ ...form, probationEndDate: e.target.value })} />
        </div>
      </div>
    );
  };

  const renderEmployeeDocumentFields = () => {
    const role = normalizeRole(isEdit ? selectedRow?.role : form.role);

    if (!["ADMIN", "MANAGER", "TRAINER"].includes(role)) {
      return <div className="alert alert-light mb-0">Documents are optional employee records.</div>;
    }

    const apiOrigin = String(api.defaults?.baseURL || "").replace(/\/api\/?$/, "");
    const documentHref = (value) => {
      if (!value) return "";
      if (/^https?:\/\//i.test(value)) return value;
      return value.startsWith("/") ? `${apiOrigin}${value}` : value;
    };

    const uploadDocument = async (event, fieldName) => {
      const file = event.target.files?.[0];
      if (!file) return;

      const data = new FormData();
      data.append("file", file);
      setUploadingField(fieldName);
      setModalError("");

      try {
        const response = await api.post("/uploads/employee-documents", data, {
          headers: { "Content-Type": "multipart/form-data" },
        });
        const storedPath = response.data?.data?.path || response.data?.data?.url || response.data?.path || response.data?.url;
        if (!storedPath) {
          throw new Error("Upload completed, but no file path was returned.");
        }
        setForm((prev) => ({ ...prev, [fieldName]: storedPath }));
      } catch (uploadError) {
        setModalError(extractApiErrorMessage(uploadError, "File upload failed"));
      } finally {
        setUploadingField("");
        event.target.value = "";
      }
    };

    const uploadHelpText = "Accepted: PDF, JPG, JPEG, PNG, WEBP. Max size: 10 MB.";

    const renderFileUpload = (label, fieldName, accept = ".pdf,.jpg,.jpeg,.png,.webp") => {
      const value = form[fieldName];
      return (
        <div className="col-md-6">
          <label className="form-label">{label}</label>
          <input
            type="file"
            className="form-control"
            accept={accept}
            disabled={uploadingField === fieldName}
            onChange={(event) => uploadDocument(event, fieldName)}
          />
          <small className="text-muted d-block mt-1">{uploadHelpText}</small>
          <small className="text-muted d-block mt-1">
            {uploadingField === fieldName
              ? "Uploading..."
              : value
                ? (
                  <a href={documentHref(value)} target="_blank" rel="noreferrer">
                    View uploaded file
                  </a>
                )
                : "No file uploaded"}
          </small>
        </div>
      );
    };

    return (
      <div className="row g-3">
        <SectionHeader label="Identity Documents" />
        <div className="col-md-6"><label className="form-label">PAN Number</label><input className="form-control" value={form.panNumber} onChange={(e) => setForm({ ...form, panNumber: e.target.value.toUpperCase() })} /></div>
        <div className="col-md-6"><label className="form-label">Aadhaar Number</label><input className="form-control" value={form.aadharNumber} onChange={(e) => setForm({ ...form, aadharNumber: e.target.value.replace(/\D/g, "") })} /></div>
        {renderFileUpload("Candidate Photo", "candidatePhotoPath", "image/*")}
        {renderFileUpload("Aadhaar Card", "aadharCardDocumentPath")}
        {renderFileUpload("PAN Card", "panCardDocumentPath")}
        {renderFileUpload("ID Proof", "idProofDocumentPath")}
        {renderFileUpload("Address Proof", "addressProofDocumentPath")}

        <SectionHeader label="Bank Details" />
        <div className="col-md-6"><label className="form-label">Account Holder Name</label><input className="form-control" value={form.bankAccountHolderName} onChange={(e) => setForm({ ...form, bankAccountHolderName: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Bank & Branch</label><input className="form-control" value={form.bankBranch} onChange={(e) => setForm({ ...form, bankBranch: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Bank Name</label><input className="form-control" value={form.bankName} onChange={(e) => setForm({ ...form, bankName: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Account Number</label><input className="form-control" value={form.bankAccountNumber} onChange={(e) => setForm({ ...form, bankAccountNumber: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">IFSC Code</label><input className="form-control" value={form.bankIfscCode} onChange={(e) => setForm({ ...form, bankIfscCode: e.target.value.toUpperCase() })} /></div>
        <div className="col-md-6"><label className="form-label">Account Type</label><input className="form-control" value={form.bankAccountType} onChange={(e) => setForm({ ...form, bankAccountType: e.target.value })} placeholder="Savings, Current" /></div>
        {renderFileUpload("Bank Proof", "bankDocumentPath")}

        <SectionHeader label="Employee Documents" />
        {renderFileUpload("Qualification Document", "qualificationDocumentPath")}
        {renderFileUpload("Certification Document", "certificationDocumentPath")}
        {renderFileUpload("Resume", "resumeDocumentPath")}
        {renderFileUpload("Offer Letter", "offerLetterDocumentPath")}

        <SectionHeader label="Employment History" />
        <div className="col-md-6"><label className="form-label">Previous Organization 1</label><input className="form-control" value={form.previousEmployment1} onChange={(e) => setForm({ ...form, previousEmployment1: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Previous Organization 2</label><input className="form-control" value={form.previousEmployment2} onChange={(e) => setForm({ ...form, previousEmployment2: e.target.value })} /></div>
        {renderFileUpload("Experience Certificate", "experienceCertificateDocumentPath")}

        <SectionHeader label="Education" />
        {renderFileUpload("Course Certificate", "courseCertificatePath")}
        {renderFileUpload("Education Certificate", "educationCertificatePath")}


        <SectionHeader label="References" />
        <div className="col-md-6"><label className="form-label">Reference Name 1</label><input className="form-control" value={form.referenceName1} onChange={(e) => setForm({ ...form, referenceName1: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Reference Phone 1</label><input className="form-control" value={form.referencePhone1} onChange={(e) => setForm({ ...form, referencePhone1: e.target.value.replace(/[^+\d]/g, "") })} /></div>
        <div className="col-md-6"><label className="form-label">Reference Name 2</label><input className="form-control" value={form.referenceName2} onChange={(e) => setForm({ ...form, referenceName2: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Reference Phone 2</label><input className="form-control" value={form.referencePhone2} onChange={(e) => setForm({ ...form, referencePhone2: e.target.value.replace(/[^+\d]/g, "") })} /></div>
        <SectionHeader label="Declaration" />
        <div className="col-md-6"><label className="form-label">Declaration Date</label><input type="date" className="form-control" value={form.declarationDate} onChange={(e) => setForm({ ...form, declarationDate: e.target.value })} /></div>
        <div className="col-md-6"><label className="form-label">Declaration Place</label><input className="form-control" value={form.declarationPlace} onChange={(e) => setForm({ ...form, declarationPlace: e.target.value })} /></div>
      </div>
    );
  };

  const renderOrgFields = () => (
    <div className="row g-3">
      <SectionHeader label="Organization Assignment" />
      <div className="col-md-6">
        <label className="form-label">Head Office</label>
        <select
          className="form-select"
          value={form.headOfficeId}
          disabled={orgLoading}
          onChange={(e) =>
            setForm({
              ...form,
              headOfficeId: e.target.value,
              branchId: "",
              departmentId: "",
              teamId: "",
              designationId: "",
            })
          }
        >
          <option value="">Select Head Office</option>
          {headOffices.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
            </option>
          ))}
        </select>
      </div>

      <div className="col-md-6">
        <label className="form-label">Branch</label>
        <select
          className="form-select"
          value={form.branchId}
          disabled={orgLoading || !form.headOfficeId}
          onChange={(e) =>
            setForm({
              ...form,
              branchId: e.target.value,
              departmentId: "",
              teamId: "",
              designationId: "",
            })
          }
        >
          <option value="">Select Branch</option>
          {filteredBranches.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
            </option>
          ))}
        </select>
      </div>

      <div className="col-md-6">
        <label className="form-label">Department</label>
        <select
          className="form-select"
          value={form.departmentId}
          disabled={orgLoading || !form.branchId}
          onChange={(e) =>
            setForm({
              ...form,
              departmentId: e.target.value,
              teamId: "",
              designationId: "",
            })
          }
        >
          <option value="">Select Department</option>
          {filteredDepartments.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
            </option>
          ))}
        </select>
      </div>

      <div className="col-md-6">
        <label className="form-label">Team</label>
        <select
          className="form-select"
          value={form.teamId}
          disabled={orgLoading || !form.departmentId}
          onChange={(e) => setForm({ ...form, teamId: e.target.value })}
        >
          <option value="">Select Team</option>
          {filteredTeams.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
            </option>
          ))}
        </select>
      </div>

      <div className="col-md-6">
        <label className="form-label">Designation</label>
        <select
          className="form-select"
          value={form.designationId}
          disabled={orgLoading || !form.departmentId}
          onChange={(e) => setForm({ ...form, designationId: e.target.value })}
        >
          <option value="">Select Designation</option>
          {filteredDesignations.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
            </option>
          ))}
        </select>
      </div>

      <div className="col-md-12">
        <div className="alert alert-light mb-0">
          <strong>Current selection:</strong> {orgPreview}
        </div>
      </div>
    </div>
  );

  const renderIdentityFields = () => {
    const effectiveRole = normalizeRole(isEdit ? selectedRow?.role : form.role);

    return (
      <div className="row g-3">
        <SectionHeader label="Access Information" />

        <div className="col-md-6">
          <label className="form-label">Role *</label>
          <select
            className="form-select"
            value={effectiveRole}
            disabled={isEdit}
            onChange={(e) => {
              const nextRole = e.target.value;
              setForm((prev) => ({
                ...createEmptyForm(nextRole),
                role: nextRole,
                email: prev.email,
                password: prev.password,
                firstName: prev.firstName,
                lastName: prev.lastName,
                name: prev.name,
                phone: prev.phone,
                countryCode: prev.countryCode,
                status: prev.status,
                headOfficeId: prev.headOfficeId,
                branchId: prev.branchId,
                departmentId: prev.departmentId,
                teamId: prev.teamId,
                designationId: prev.designationId,
              }));
            }}
          >
            {roleSelectOptions.map((item) => (
              <option key={item.value} value={item.value} disabled={item.disabled}>
                {item.label}
              </option>
            ))}
          </select>
          <small className="text-muted">
            {isEdit
              ? "Role changes are locked during edit because each role uses a different backend route."
              : "Only the roles supported by the backend are enabled for your current login."}
          </small>
        </div>

        <div className="col-md-6">
          <label className="form-label">Status</label>
          <select
            className="form-select"
            value={form.status}
            onChange={(e) => setForm({ ...form, status: e.target.value })}
          >
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </div>

        <div className="col-md-6">
          <label className="form-label">Name *</label>
          <input
            className="form-control"
            value={form.name}
            onChange={(e) => {
              const nextName = e.target.value;
              const split = splitName(nextName);
              setForm({ ...form, name: nextName, firstName: split.firstName, lastName: split.lastName });
            }}
          />
        </div>

        <div className="col-md-6">
          <label className="form-label">Email *</label>
          <input
            type="email"
            className="form-control"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
        </div>

        <div className="col-md-6">
          <label className="form-label">{isEdit ? "Password" : "Password *"}</label>
          <input
            type="password"
            className="form-control"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            placeholder={isEdit ? "Leave blank to keep current password" : "Set an initial password"}
          />
        </div>

        <div className="col-md-6">
          <label className="form-label">Phone</label>
          <div className="input-group">
            <div className="position-relative" ref={countryDropdownRef}>
              <button
                type="button"
                className="btn btn-outline-secondary dropdown-toggle"
                onClick={() => setCountryDropdownOpen((value) => !value)}
              >
                {ensureCountryCodeValue(form.countryCode)}
              </button>
              {countryDropdownOpen && (
                <div
                  className="dropdown-menu show p-2"
                  style={{ width: 280, maxHeight: 280, overflowY: "auto" }}
                >
                  <input
                    className="form-control form-control-sm mb-2"
                    value={countrySearch}
                    onChange={(e) => setCountrySearch(e.target.value)}
                    placeholder="Search country code"
                  />
                  {countryOptions.map((opt) => (
                    <button
                      key={`${opt.value}-${opt.label}`}
                      type="button"
                      className="dropdown-item"
                      onClick={() => {
                        setForm({ ...form, countryCode: opt.value });
                        setCountryDropdownOpen(false);
                        setCountrySearch("");
                      }}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              )}
            </div>
            <input
              className="form-control"
              value={form.phone}
              onChange={(e) => setForm({ ...form, phone: e.target.value.replace(/\D/g, "") })}
            />
          </div>
        </div>

        <div className="col-md-6">
          <label className="form-label">Employee Code</label>
          <input
            className="form-control"
            value={form.employeeCode}
            onChange={(e) => setForm({ ...form, employeeCode: e.target.value })}
          />
        </div>

        <div className="col-md-12">
          <div className="alert alert-info mb-0">
            {currentRole === "SUPER_ADMIN" && "Super Admin can create and manage Admin, Manager, Trainer, and User records. Same-level Super Admin records are hidden and blocked."}
            {currentRole === "ADMIN" && "Admin can create and manage Manager, Trainer, and User records from this page."}
            {currentRole === "MANAGER" && "Manager can create and manage Trainer and User records from this page."}
            {currentRole === "TRAINER" && "Trainer can create User records from this page."}
            {!["SUPER_ADMIN", "ADMIN", "MANAGER", "TRAINER"].includes(currentRole) &&
              "This page is read-only for your current role."}
          </div>
        </div>
      </div>
    );
  };

  const renderUserModal = () =>
    showModal && (
      <Modal show={showModal} onHide={() => setShowModal(false)} size="xl" centered>
        <Form onSubmit={handleSubmit}>
          <Modal.Header closeButton>
            <Modal.Title>{isEdit ? `Edit ${normalizeRole(form.role) === "USER" ? "User" : "Employee"}` : `Add ${normalizeRole(form.role) === "USER" ? "User" : "Employee"}`}</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {modalError && <div className="alert alert-danger">{modalError}</div>}

            {renderTabs()}

            {modalTab === "identity" && renderIdentityFields()}
            {modalTab === "organization" && renderOrgFields()}
            {modalTab === "details" && renderRoleSpecificFields()}
            {modalTab === "personal" && renderEmployeePersonalFields()}
            {modalTab === "documents" && renderEmployeeDocumentFields()}
          </Modal.Body>

          <Modal.Footer>
            <Button variant="light" onClick={() => setShowModal(false)} type="button">
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Save Changes"}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    );

  const renderDeleteModal = () =>
    deleteTarget && (
      <Modal show={Boolean(deleteTarget)} onHide={() => setDeleteTarget(null)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Confirm Delete</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>
            Are you sure you want to delete <strong>{deleteTarget?.name || "this record"}</strong>?
            This action cannot be undone.
          </p>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="light" onClick={() => setDeleteTarget(null)} type="button">
            Cancel
          </Button>
          <Button variant="danger" onClick={handleDelete} disabled={saving} type="button">
            {saving ? "Deleting..." : "Delete"}
          </Button>
        </Modal.Footer>
      </Modal>
    );

  const scopeHelpers = {
    headOffices,
    branches,
    departments,
    teams,
    designations,
  };

  const renderGridCard = (row) => (
    <div className="col-xl-3 col-lg-4 col-md-6 d-flex" key={`${row.role}-${row.id}`}>
      <div className="card flex-fill">
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-start mb-3">
            <span className={`badge ${getRoleBadgeClass(row.role)}`}>{row.role}</span>
            <div className="d-flex gap-1">
              <button className="btn btn-sm btn-outline-primary" onClick={() => openEdit(row)} type="button">
                <IconEdit size={14} />
              </button>
              <button className="btn btn-sm btn-outline-danger" onClick={() => confirmDelete(row)} type="button">
                <IconTrash size={14} />
              </button>
            </div>
          </div>

          <div className="d-flex align-items-center mb-3">
            <img
              src={row.img || ROLE_AVATARS[normalizeRole(row.role)] || userAvatar}
              alt={row.name}
              className="rounded-circle me-2"
              width="42"
              height="42"
            />
            <div>
              <h6 className="mb-0">{row.name || "-"}</h6>
              <small className="text-muted">{row.department || "-"}</small>
            </div>
          </div>

          <p className="mb-1">Email: {row.email || "-"}</p>
          <p className="mb-1">Phone: {formatPhoneWithCode(row.phone, row.countryCode)}</p>
          <p className="mb-1">Org: {buildOrgLabel(row.raw, scopeHelpers)}</p>
          <span className={`badge ${row.status === "ACTIVE" ? "bg-success" : "bg-danger"}`}>
            {row.status === "ACTIVE" ? "Active" : "Inactive"}
          </span>
        </div>
      </div>
    </div>
  );

  if (gridView) {
    return (
      <div className="content">
        {notice && <div className="alert alert-success">{notice}</div>}
        {error && <div className="alert alert-danger">{error}</div>}

        {renderHeader("User Management", "Users Grid")}
        {renderDisplaySwitch()}
        {renderStats()}

        <div className="row">
          {loading ? (
            <div className="text-center py-4">Loading...</div>
          ) : displayedRows.length === 0 ? (
            <div className="text-center py-4">
              No {viewMode === "users" ? "users" : "employees"} found for your role.
            </div>
          ) : (
            displayedRows.map((row) => renderGridCard(row))
          )}
        </div>

        {renderUserModal()}
        {renderDeleteModal()}
      </div>
    );
  }

  return (
    <div className="page-wrapper users-page-wrapper">
      <div className="content">
        {notice && <div className="alert alert-success">{notice}</div>}
        {error && <div className="alert alert-danger">{error}</div>}

        {renderHeader("User Management", "Users")}
        {renderDisplaySwitch()}
        {renderStats()}

        <div className="card mb-3">
          <div className="card-body d-flex flex-wrap align-items-center gap-3 justify-content-between">
            <div>
              <h5 className="mb-1">Current Role</h5>
              <p className="text-muted mb-0">
                Logged in as <strong>{currentRole || "UNKNOWN"}</strong>
                {currentUserId ? ` (User ID: ${currentUserId})` : ""}
              </p>
            </div>
            <div className="d-flex flex-wrap gap-2">
              {ROLE_OPTIONS.map((item) => (
                <span key={item.value} className={`badge ${getRoleBadgeClass(item.value)}`}>
                  {item.label}
                </span>
              ))}
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
            <h5 className="mb-0">{viewMode === "users" ? "Users List" : "Employee List"}</h5>
            <div className="d-flex flex-wrap gap-2 align-items-center">
              <span className="badge bg-info">{currentRole || "UNKNOWN"}</span>
              <span className="badge bg-light text-dark">
                Create:{" "}
                {creatableRolesForCurrentView.map((item) => item.label).join(", ") || "None"}
              </span>
            </div>
          </div>

          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-striped table-hover mb-0">
                <thead className="thead-light">
                  <tr>
                    <th>Role</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Department / Title</th>
                    <th>Org Scope</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan={8} className="text-center py-4">
                        Loading...
                      </td>
                    </tr>
                  ) : displayedRows.length === 0 ? (
                    <tr>
                      <td colSpan={8} className="text-center py-4">
                        No {viewMode === "users" ? "users" : "employees"} found for your role.
                      </td>
                    </tr>
                  ) : (
                    displayedRows.map((row) => (
                      <tr key={`${row.role}-${row.id}`}>
                        <td>
                          <span className={`badge ${getRoleBadgeClass(row.role)}`}>{row.role}</span>
                        </td>
                        <td>
                          <div className="d-flex align-items-center">
                            <img
                              src={row.img || ROLE_AVATARS[normalizeRole(row.role)] || userAvatar}
                              alt={row.name}
                              className="rounded-circle me-2"
                              width="34"
                              height="34"
                            />
                            <span className="fw-semibold">{row.name || "-"}</span>
                          </div>
                        </td>
                        <td>{row.email || "-"}</td>
                        <td>{formatPhoneWithCode(row.phone, row.countryCode)}</td>
                        <td>{row.department || "-"}</td>
                        <td>{buildOrgLabel(row.raw, scopeHelpers)}</td>
                        <td>
                          <span className={`badge ${row.status === "ACTIVE" ? "bg-success" : "bg-danger"}`}>
                            {row.status === "ACTIVE" ? "Active" : "Inactive"}
                          </span>
                        </td>
                        <td>
                          <button
                            className="btn btn-sm btn-outline-primary me-1"
                            onClick={() => openEdit(row)}
                            type="button"
                          >
                            <IconEdit size={14} />
                          </button>
                          <button
                            className="btn btn-sm btn-outline-danger"
                            onClick={() => confirmDelete(row)}
                            type="button"
                          >
                            <IconTrash size={14} />
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {renderUserModal()}
        {renderDeleteModal()}
      </div>
    </div>
  );
}






























