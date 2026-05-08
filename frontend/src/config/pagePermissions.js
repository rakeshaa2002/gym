const ROLE_ORDER = ["SUPER_ADMIN", "ADMIN", "MANAGER", "TRAINER", "USER"];

const APP_PAGES = [
  { key: "dashboard", label: "Overview", path: "/", category: "Core", sortOrder: 1 },
  { key: "role-permissions", label: "Role Permissions", path: "/role-permissions", category: "Management", sortOrder: 5 },
  { key: "employees", label: "Employees", path: "/employees", category: "Management", sortOrder: 10 },
  { key: "users", label: "Users", path: "/users", category: "Management", sortOrder: 11 },
  { key: "headoffice", label: "Head Office", path: "/headoffice", category: "Management", sortOrder: 12 },
  { key: "branches", label: "Branches", path: "/branches", category: "Management", sortOrder: 13 },
  { key: "departments", label: "Departments", path: "/departments", category: "Management", sortOrder: 14 },
  { key: "designations", label: "Designations", path: "/designations", category: "Management", sortOrder: 15 },
  { key: "teams", label: "Teams", path: "/teams", category: "Management", sortOrder: 16 },
  { key: "workout-filter", label: "Workout Filter", path: "/workout-filter", category: "Workout", sortOrder: 20 },
  { key: "workout-topfilter", label: "Workout Top Filter", path: "/workout-topfilter", category: "Workout", sortOrder: 21 },
  { key: "upperbody-workout", label: "Body Workout", path: "/upperbody-workout", category: "Workout", sortOrder: 22 },
  { key: "create-workout", label: "Create Workout", path: "/create-workout", category: "Workout", sortOrder: 23 },
  { key: "workout-summary", label: "Workout Summary", path: "/workout-summary", category: "Workout", sortOrder: 24 },
  { key: "workout-type", label: "Workout Type Master", path: "/workout-type", category: "Workout", sortOrder: 25 },
  { key: "body-part", label: "Body Part Master", path: "/body-part", category: "Workout", sortOrder: 26 },
  { key: "exercise-master", label: "Exercise Master", path: "/exercise-master", category: "Workout", sortOrder: 27 },
  { key: "workout-plan", label: "Workout Plan Master", path: "/workout-plan", category: "Workout", sortOrder: 28 },
  { key: "workout-detail", label: "Workout Detail", path: "/workout-detail", category: "Workout", sortOrder: 29 },
  { key: "trainer-duty-schedule", label: "Trainer Duty Schedule", path: "/trainer-duty-schedule", category: "Schedule", sortOrder: 35 },
  { key: "user-workout-schedule", label: "User Workout Schedule", path: "/user-workout-schedule", category: "Schedule", sortOrder: 36 },
  { key: "my-schedule", label: "My Schedule", path: "/my-schedule", category: "Schedule", sortOrder: 37 },
  { key: "dietplan", label: "Diet Menu", path: "/dietplan", category: "Diet Menu", sortOrder: 30 },
  { key: "diet-detail", label: "Diet Detail", path: "/diet-detail", category: "Diet Menu", sortOrder: 31 },
  { key: "goals", label: "Goals", path: "/goals", category: "Fitness", sortOrder: 40 },
  { key: "schedule", label: "My Schedule", path: "/schedule", category: "Fitness", sortOrder: 41 },
  { key: "progress", label: "Progress", path: "/progress", category: "Fitness", sortOrder: 42 },
  { key: "profile", label: "Profile", path: "/profile", category: "Account", sortOrder: 50 },
  { key: "onboding-step", label: "Step", path: "/onboding-step", category: "Account", sortOrder: 51 },
];

const ROLE_LABELS = {
  SUPER_ADMIN: "Super Admin",
  ADMIN: "Admin",
  MANAGER: "Manager",
  TRAINER: "Trainer",
  USER: "User",
};

const ROLE_DEFAULT_PAGES = {
  SUPER_ADMIN: APP_PAGES.map((page) => page.key),
  ADMIN: ["dashboard", "employees", "users", "headoffice", "branches", "departments", "designations", "teams", "workout-type", "body-part", "exercise-master", "workout-plan", "workout-detail", "trainer-duty-schedule", "user-workout-schedule", "schedule", "profile"],
  MANAGER: ["dashboard", "employees", "users", "branches", "departments", "designations", "teams", "workout-type", "body-part", "exercise-master", "workout-plan", "workout-detail", "trainer-duty-schedule", "user-workout-schedule", "schedule", "profile"],
  TRAINER: ["dashboard", "users", "workout-filter", "workout-topfilter", "upperbody-workout", "create-workout", "workout-summary", "workout-type", "body-part", "exercise-master", "workout-plan", "workout-detail", "trainer-duty-schedule", "user-workout-schedule", "dietplan", "diet-detail", "goals", "schedule", "progress", "profile"],
  USER: ["dashboard", "workout-detail", "dietplan", "diet-detail", "goals", "schedule", "my-schedule", "progress", "profile", "onboding-step"],
};

function normalizeRole(role) {
  return String(role || "").trim().toUpperCase();
}

function getDefaultRolePermissions(role) {
  const normalized = normalizeRole(role);
  const allowedPages = new Set(ROLE_DEFAULT_PAGES[normalized] || []);

  return APP_PAGES.reduce((acc, page) => {
    acc[page.key] = {
      pageKey: page.key,
      canView: allowedPages.has(page.key),
      canCreate: normalized === "SUPER_ADMIN" ? true : false,
      canEdit: normalized === "SUPER_ADMIN" ? true : false,
      canDelete: normalized === "SUPER_ADMIN" ? true : false,
    };
    return acc;
  }, {});
}

function buildPermissionMap(permissions = []) {
  return (Array.isArray(permissions) ? permissions : []).reduce((acc, permission) => {
    if (!permission?.pageKey) return acc;
    acc[permission.pageKey] = {
      pageKey: permission.pageKey,
      canView: Boolean(permission.canView),
      canCreate: Boolean(permission.canCreate),
      canEdit: Boolean(permission.canEdit),
      canDelete: Boolean(permission.canDelete),
    };
    return acc;
  }, {});
}

function toPermissionList(permissionMap = {}) {
  return APP_PAGES.map((page) => {
    const existing = permissionMap[page.key] || {};
    return {
      pageKey: page.key,
      canView: Boolean(existing.canView),
      canCreate: Boolean(existing.canCreate),
      canEdit: Boolean(existing.canEdit),
      canDelete: Boolean(existing.canDelete),
    };
  });
}

function findPageByPath(pathname) {
  const normalized = String(pathname || "").replace(/\/+$/, "") || "/";
  return APP_PAGES.find((page) => page.path === normalized) || null;
}

function canAccess(permissionMap, pageKey, action = "view") {
  const permission = permissionMap?.[pageKey];
  if (!permission) return false;

  switch (String(action || "view").toLowerCase()) {
    case "create":
      return Boolean(permission.canCreate);
    case "edit":
      return Boolean(permission.canEdit);
    case "delete":
      return Boolean(permission.canDelete);
    default:
      return Boolean(permission.canView);
  }
}

function groupPages() {
  return APP_PAGES.reduce((acc, page) => {
    if (!acc[page.category]) acc[page.category] = [];
    acc[page.category].push(page);
    return acc;
  }, {});
}

export {
  APP_PAGES,
  ROLE_ORDER,
  ROLE_LABELS,
  ROLE_DEFAULT_PAGES,
  buildPermissionMap,
  canAccess,
  findPageByPath,
  getDefaultRolePermissions,
  groupPages,
  normalizeRole,
  toPermissionList,
};
