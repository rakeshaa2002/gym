-- Permission catalog and role permissions seed
-- Run after existing manual seeds if you want the default permission matrix.

BEGIN;

CREATE TABLE IF NOT EXISTS permission_pages (
    id BIGSERIAL PRIMARY KEY,
    page_key VARCHAR(100) NOT NULL UNIQUE,
    label VARCHAR(150) NOT NULL,
    route_path VARCHAR(150) NOT NULL,
    category VARCHAR(80) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS role_page_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(20) NOT NULL,
    page_key VARCHAR(100) NOT NULL,
    can_view BOOLEAN NOT NULL DEFAULT FALSE,
    can_create BOOLEAN NOT NULL DEFAULT FALSE,
    can_edit BOOLEAN NOT NULL DEFAULT FALSE,
    can_delete BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_role_page_permission UNIQUE (role_name, page_key)
);

INSERT INTO permission_pages (page_key, label, route_path, category, sort_order) VALUES
('dashboard', 'Overview', '/', 'Core', 1),
('role-permissions', 'Role Permissions', '/role-permissions', 'Management', 5),
('employees', 'Employees', '/employees', 'Management', 10),
('users', 'Users', '/users', 'Management', 11),
('headoffice', 'Head Office', '/headoffice', 'Management', 12),
('branches', 'Branches', '/branches', 'Management', 13),
('departments', 'Departments', '/departments', 'Management', 14),
('designations', 'Designations', '/designations', 'Management', 15),
('teams', 'Teams', '/teams', 'Management', 16),
('workout-filter', 'Workout Filter', '/workout-filter', 'Workout', 20),
('workout-topfilter', 'Workout Top Filter', '/workout-topfilter', 'Workout', 21),
('upperbody-workout', 'Body Workout', '/upperbody-workout', 'Workout', 22),
('create-workout', 'Create Workout', '/create-workout', 'Workout', 23),
('workout-summary', 'Workout Summary', '/workout-summary', 'Workout', 24),
('dietplan', 'Diet Menu', '/dietplan', 'Diet Plan', 30),
('diet-detail', 'Diet Detail', '/diet-detail', 'Diet Plan', 31),
('goals', 'Goals', '/goals', 'Fitness', 40),
('schedule', 'My Schedule', '/schedule', 'Fitness', 41),
('progress', 'Progress', '/progress', 'Fitness', 42),
('profile', 'Profile', '/profile', 'Account', 50)
('onboding-step', 'Step', '/onboding-step', 'Account', 51)
ON CONFLICT (page_key) DO UPDATE SET
    label = EXCLUDED.label,
    route_path = EXCLUDED.route_path,
    category = EXCLUDED.category,
    sort_order = EXCLUDED.sort_order;

DELETE FROM role_page_permissions;

INSERT INTO role_page_permissions (role_name, page_key, can_view, can_create, can_edit, can_delete) VALUES
('SUPER_ADMIN', 'dashboard', true, true, true, true),
('SUPER_ADMIN', 'employees', true, true, true, true),
('SUPER_ADMIN', 'users', true, true, true, true),
('SUPER_ADMIN', 'headoffice', true, true, true, true),
('SUPER_ADMIN', 'branches', true, true, true, true),
('SUPER_ADMIN', 'departments', true, true, true, true),
('SUPER_ADMIN', 'designations', true, true, true, true),
('SUPER_ADMIN', 'teams', true, true, true, true),
('SUPER_ADMIN', 'workout-filter', true, true, true, true),
('SUPER_ADMIN', 'workout-topfilter', true, true, true, true),
('SUPER_ADMIN', 'upperbody-workout', true, true, true, true),
('SUPER_ADMIN', 'create-workout', true, true, true, true),
('SUPER_ADMIN', 'workout-summary', true, true, true, true),
('SUPER_ADMIN', 'dietplan', true, true, true, true),
('SUPER_ADMIN', 'diet-detail', true, true, true, true),
('SUPER_ADMIN', 'goals', true, true, true, true),
('SUPER_ADMIN', 'schedule', true, true, true, true),
('SUPER_ADMIN', 'progress', true, true, true, true),
('SUPER_ADMIN', 'profile', true, true, true, true),
('SUPER_ADMIN', 'onboding-step', true, true, true, true),
('SUPER_ADMIN', 'role-permissions', true, true, true, true),

('ADMIN', 'dashboard', true, false, false, false),
('ADMIN', 'employees', true, true, true, true),
('ADMIN', 'users', true, true, true, true),
('ADMIN', 'headoffice', true, true, true, true),
('ADMIN', 'branches', true, true, true, true),
('ADMIN', 'departments', true, true, true, true),
('ADMIN', 'designations', true, true, true, true),
('ADMIN', 'teams', true, true, true, true),
('ADMIN', 'schedule', true, false, false, false),
('ADMIN', 'profile', true, false, false, false),
('ADMIN', 'onboding-step', true, false, false, false),

('MANAGER', 'dashboard', true, false, false, false),
('MANAGER', 'employees', true, true, true, true),
('MANAGER', 'users', true, true, true, true),
('MANAGER', 'branches', true, true, true, true),
('MANAGER', 'departments', true, true, true, true),
('MANAGER', 'designations', true, true, true, true),
('MANAGER', 'teams', true, true, true, true),
('MANAGER', 'schedule', true, false, false, false),
('MANAGER', 'profile', true, false, false, false),
('MANAGER', 'onboding-step', true, false, false, false),

('TRAINER', 'dashboard', true, false, false, false),
('TRAINER', 'users', true, true, true, true),
('TRAINER', 'workout-filter', true, false, false, false),
('TRAINER', 'workout-topfilter', true, false, false, false),
('TRAINER', 'upperbody-workout', true, false, false, false),
('TRAINER', 'create-workout', true, true, true, true),
('TRAINER', 'workout-summary', true, false, false, false),
('TRAINER', 'dietplan', true, false, false, false),
('TRAINER', 'diet-detail', true, false, false, false),
('TRAINER', 'goals', true, false, false, false),
('TRAINER', 'schedule', true, false, false, false),
('TRAINER', 'progress', true, false, false, false),
('TRAINER', 'profile', true, false, false, false),
('TRAINER', 'onboding-step', true, false, false, false),

('USER', 'dashboard', true, false, false, false),
('USER', 'dietplan', true, false, false, false),
('USER', 'diet-detail', true, false, false, false),
('USER', 'goals', true, false, false, false),
('USER', 'schedule', true, false, false, false),
('USER', 'progress', true, false, false, false),
('USER', 'profile', true, false, false, false),
('USER', 'onboding-step', true, false, false, false);

COMMIT;
