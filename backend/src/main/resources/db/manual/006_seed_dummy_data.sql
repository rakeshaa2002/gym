-- Manual PostgreSQL seed data for local development/demo.
-- Run after migrations 001 through 005:
--   psql -U postgres -d fitnexus -f src/main/resources/db/manual/006_seed_dummy_data.sql
--
-- All dummy accounts use password: Admin@123
-- The visible dummy data is intentionally simple: Head Office 1, Branch 1,
-- Department 1, Team 1, Admin 1, User 1, etc.

BEGIN;

CREATE SEQUENCE IF NOT EXISTS user_accounts_id_seq OWNED BY user_accounts.id;
ALTER TABLE user_accounts ALTER COLUMN id SET DEFAULT nextval('user_accounts_id_seq');

-- Rename older realistic seed records if this script was run before.
UPDATE head_offices SET name = 'Head Office 1', location = 'Location 1', address = 'Address 1', phone = '+910000000001', email = 'headoffice1@fitnexus.test', status = 'ACTIVE', updated_at = NOW() WHERE name = 'FitNexus Corporate HQ' OR email = 'hq@fitnexus.test';
UPDATE head_offices SET name = 'Head Office 2', location = 'Location 2', address = 'Address 2', phone = '+910000000002', email = 'headoffice2@fitnexus.test', status = 'ACTIVE', updated_at = NOW() WHERE name = 'FitNexus West HQ' OR email = 'westhq@fitnexus.test';
UPDATE head_offices SET name = 'Head Office 3', location = 'Location 3', address = 'Address 3', phone = '+910000000003', email = 'headoffice3@fitnexus.test', status = 'ACTIVE', updated_at = NOW() WHERE name = 'FitNexus North HQ' OR email = 'northhq@fitnexus.test';

UPDATE branches SET name = 'Branch 1', location = 'Location 1', address = 'Branch Address 1', phone = '+911000000001', email = 'branch1@fitnexus.test', manager_name = 'Manager 1', status = 'ACTIVE', updated_at = NOW() WHERE name = 'Coimbatore Main Branch' OR email = 'cbe.main@fitnexus.test';
UPDATE branches SET name = 'Branch 2', location = 'Location 2', address = 'Branch Address 2', phone = '+911000000002', email = 'branch2@fitnexus.test', manager_name = 'Manager 2', status = 'ACTIVE', updated_at = NOW() WHERE name = 'Tiruppur Training Branch' OR email = 'tiruppur@fitnexus.test';
UPDATE branches SET name = 'Branch 3', location = 'Location 3', address = 'Branch Address 3', phone = '+911000000003', email = 'branch3@fitnexus.test', manager_name = 'Manager 3', status = 'ACTIVE', updated_at = NOW() WHERE name = 'Erode Strength Branch' OR email = 'erode@fitnexus.test';
UPDATE branches SET name = 'Branch 4', location = 'Location 4', address = 'Branch Address 4', phone = '+911000000004', email = 'branch4@fitnexus.test', manager_name = 'Manager 4', status = 'ACTIVE', updated_at = NOW() WHERE name = 'Salem Wellness Branch' OR email = 'salem@fitnexus.test';

UPDATE departments SET name = 'Department 1', description = 'Department 1 description', status = 'ACTIVE', updated_at = NOW() WHERE name = 'Operations';
UPDATE departments SET name = 'Department 2', description = 'Department 2 description', status = 'ACTIVE', updated_at = NOW() WHERE name = 'Fitness Training';
UPDATE departments SET name = 'Department 3', description = 'Department 3 description', status = 'ACTIVE', updated_at = NOW() WHERE name = 'Customer Success';
UPDATE departments SET name = 'Department 4', description = 'Department 4 description', status = 'ACTIVE', updated_at = NOW() WHERE name = 'Wellness Programs';

UPDATE designations SET name = 'Designation 1', description = 'Designation 1 description', level = 'JUNIOR', salary = 25000, status = 'ACTIVE', updated_at = NOW() WHERE name = 'Branch Admin';
UPDATE designations SET name = 'Designation 2', description = 'Designation 2 description', level = 'SENIOR', salary = 35000, status = 'ACTIVE', updated_at = NOW() WHERE name = 'Fitness Manager';
UPDATE designations SET name = 'Designation 3', description = 'Designation 3 description', level = 'LEAD', salary = 45000, status = 'ACTIVE', updated_at = NOW() WHERE name = 'Senior Trainer';
UPDATE designations SET name = 'Designation 4', description = 'Designation 4 description', level = 'MANAGER', salary = 55000, status = 'ACTIVE', updated_at = NOW() WHERE name = 'Customer Executive';

UPDATE teams SET name = 'Team 1', description = 'Team 1 description', team_lead = 'Team Lead 1', member_count = 2, status = 'ACTIVE', updated_at = NOW() WHERE name = 'Morning Operations';
UPDATE teams SET name = 'Team 2', description = 'Team 2 description', team_lead = 'Team Lead 2', member_count = 3, status = 'ACTIVE', updated_at = NOW() WHERE name = 'Strength Squad';
UPDATE teams SET name = 'Team 3', description = 'Team 3 description', team_lead = 'Team Lead 3', member_count = 4, status = 'ACTIVE', updated_at = NOW() WHERE name = 'Member Care';
UPDATE teams SET name = 'Team 4', description = 'Team 4 description', team_lead = 'Team Lead 4', member_count = 4, status = 'ACTIVE', updated_at = NOW() WHERE name = 'Yoga Circle';

-- -----------------------------------------------------------------------------
-- Organization master data
-- -----------------------------------------------------------------------------

INSERT INTO head_offices (name, location, address, phone, email, status, created_at, updated_at)
VALUES
    ('Head Office 1', 'Location 1', 'Address 1', '+910000000001', 'headoffice1@fitnexus.test', 'ACTIVE', NOW(), NOW()),
    ('Head Office 2', 'Location 2', 'Address 2', '+910000000002', 'headoffice2@fitnexus.test', 'ACTIVE', NOW(), NOW()),
    ('Head Office 3', 'Location 3', 'Address 3', '+910000000003', 'headoffice3@fitnexus.test', 'ACTIVE', NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET
    location = EXCLUDED.location,
    address = EXCLUDED.address,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    status = EXCLUDED.status,
    updated_at = NOW();

INSERT INTO branches (name, head_office_id, location, address, phone, email, manager_name, status, created_at, updated_at)
SELECT 'Branch 1', ho.id, 'Location 1', 'Branch Address 1', '+911000000001', 'branch1@fitnexus.test', 'Manager 1', 'ACTIVE', NOW(), NOW()
FROM head_offices ho WHERE ho.name = 'Head Office 1'
AND NOT EXISTS (SELECT 1 FROM branches b WHERE b.name = 'Branch 1')
UNION ALL
SELECT 'Branch 2', ho.id, 'Location 2', 'Branch Address 2', '+911000000002', 'branch2@fitnexus.test', 'Manager 2', 'ACTIVE', NOW(), NOW()
FROM head_offices ho WHERE ho.name = 'Head Office 1'
AND NOT EXISTS (SELECT 1 FROM branches b WHERE b.name = 'Branch 2')
UNION ALL
SELECT 'Branch 3', ho.id, 'Location 3', 'Branch Address 3', '+911000000003', 'branch3@fitnexus.test', 'Manager 3', 'ACTIVE', NOW(), NOW()
FROM head_offices ho WHERE ho.name = 'Head Office 2'
AND NOT EXISTS (SELECT 1 FROM branches b WHERE b.name = 'Branch 3')
UNION ALL
SELECT 'Branch 4', ho.id, 'Location 4', 'Branch Address 4', '+911000000004', 'branch4@fitnexus.test', 'Manager 4', 'ACTIVE', NOW(), NOW()
FROM head_offices ho WHERE ho.name = 'Head Office 3'
AND NOT EXISTS (SELECT 1 FROM branches b WHERE b.name = 'Branch 4');

INSERT INTO departments (name, branch_id, description, status, created_at, updated_at)
SELECT 'Department 1', b.id, 'Department 1 description', 'ACTIVE', NOW(), NOW()
FROM branches b WHERE b.name = 'Branch 1'
AND NOT EXISTS (SELECT 1 FROM departments d WHERE d.name = 'Department 1' AND d.branch_id = b.id)
UNION ALL
SELECT 'Department 2', b.id, 'Department 2 description', 'ACTIVE', NOW(), NOW()
FROM branches b WHERE b.name = 'Branch 1'
AND NOT EXISTS (SELECT 1 FROM departments d WHERE d.name = 'Department 2' AND d.branch_id = b.id)
UNION ALL
SELECT 'Department 3', b.id, 'Department 3 description', 'ACTIVE', NOW(), NOW()
FROM branches b WHERE b.name = 'Branch 3'
AND NOT EXISTS (SELECT 1 FROM departments d WHERE d.name = 'Department 3' AND d.branch_id = b.id)
UNION ALL
SELECT 'Department 4', b.id, 'Department 4 description', 'ACTIVE', NOW(), NOW()
FROM branches b WHERE b.name = 'Branch 4'
AND NOT EXISTS (SELECT 1 FROM departments d WHERE d.name = 'Department 4' AND d.branch_id = b.id);

INSERT INTO designations (name, department_id, description, level, salary, status, created_at, updated_at)
SELECT 'Designation 1', d.id, 'Designation 1 description', 'JUNIOR', 25000, 'ACTIVE', NOW(), NOW()
FROM departments d JOIN branches b ON b.id = d.branch_id WHERE d.name = 'Department 1' AND b.name = 'Branch 1'
AND NOT EXISTS (SELECT 1 FROM designations x WHERE x.name = 'Designation 1' AND x.department_id = d.id)
UNION ALL
SELECT 'Designation 2', d.id, 'Designation 2 description', 'SENIOR', 35000, 'ACTIVE', NOW(), NOW()
FROM departments d JOIN branches b ON b.id = d.branch_id WHERE d.name = 'Department 2' AND b.name = 'Branch 1'
AND NOT EXISTS (SELECT 1 FROM designations x WHERE x.name = 'Designation 2' AND x.department_id = d.id)
UNION ALL
SELECT 'Designation 3', d.id, 'Designation 3 description', 'LEAD', 45000, 'ACTIVE', NOW(), NOW()
FROM departments d JOIN branches b ON b.id = d.branch_id WHERE d.name = 'Department 2' AND b.name = 'Branch 1'
AND NOT EXISTS (SELECT 1 FROM designations x WHERE x.name = 'Designation 3' AND x.department_id = d.id)
UNION ALL
SELECT 'Designation 4', d.id, 'Designation 4 description', 'MANAGER', 55000, 'ACTIVE', NOW(), NOW()
FROM departments d JOIN branches b ON b.id = d.branch_id WHERE d.name = 'Department 3' AND b.name = 'Branch 3'
AND NOT EXISTS (SELECT 1 FROM designations x WHERE x.name = 'Designation 4' AND x.department_id = d.id);

INSERT INTO teams (name, department_id, description, team_lead, member_count, status, created_at, updated_at)
SELECT 'Team 1', d.id, 'Team 1 description', 'Team Lead 1', 2, 'ACTIVE', NOW(), NOW()
FROM departments d JOIN branches b ON b.id = d.branch_id WHERE d.name = 'Department 1' AND b.name = 'Branch 1'
AND NOT EXISTS (SELECT 1 FROM teams t WHERE t.name = 'Team 1' AND t.department_id = d.id)
UNION ALL
SELECT 'Team 2', d.id, 'Team 2 description', 'Team Lead 2', 3, 'ACTIVE', NOW(), NOW()
FROM departments d JOIN branches b ON b.id = d.branch_id WHERE d.name = 'Department 2' AND b.name = 'Branch 1'
AND NOT EXISTS (SELECT 1 FROM teams t WHERE t.name = 'Team 2' AND t.department_id = d.id)
UNION ALL
SELECT 'Team 3', d.id, 'Team 3 description', 'Team Lead 3', 4, 'ACTIVE', NOW(), NOW()
FROM departments d JOIN branches b ON b.id = d.branch_id WHERE d.name = 'Department 3' AND b.name = 'Branch 3'
AND NOT EXISTS (SELECT 1 FROM teams t WHERE t.name = 'Team 3' AND t.department_id = d.id)
UNION ALL
SELECT 'Team 4', d.id, 'Team 4 description', 'Team Lead 4', 4, 'ACTIVE', NOW(), NOW()
FROM departments d JOIN branches b ON b.id = d.branch_id WHERE d.name = 'Department 4' AND b.name = 'Branch 4'
AND NOT EXISTS (SELECT 1 FROM teams t WHERE t.name = 'Team 4' AND t.department_id = d.id);

-- -----------------------------------------------------------------------------
-- Identity accounts and role profiles
-- -----------------------------------------------------------------------------

CREATE TEMP TABLE seed_people (
    email TEXT PRIMARY KEY,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    role TEXT NOT NULL,
    creator_email TEXT NULL,
    reports_to_email TEXT NULL,
    head_office_name TEXT NULL,
    branch_name TEXT NULL,
    department_name TEXT NULL,
    team_name TEXT NULL,
    designation_name TEXT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NULL,
    phone TEXT NULL,
    employee_id TEXT NULL,
    qualification TEXT NULL,
    specialization TEXT NULL,
    experience_years INTEGER NULL,
    certification TEXT NULL,
    rate_per_hour DOUBLE PRECISION NULL,
    gender TEXT NULL,
    blood_group TEXT NULL,
    city TEXT NULL,
    address TEXT NULL,
    fitness_goals TEXT NULL,
    dietary_preferences TEXT NULL
) ON COMMIT DROP;

INSERT INTO seed_people VALUES
    ('super1@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'Super Admin 1', 'SUPER_ADMIN', NULL, NULL, 'Head Office 1', NULL, NULL, NULL, NULL, 'Super Admin 1', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Male', 'O+', 'City 1', NULL, NULL, NULL),
    ('super2@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'Super Admin 2', 'SUPER_ADMIN', 'super1@fitnexus.test', NULL, 'Head Office 1', NULL, NULL, NULL, NULL, 'Super Admin 2', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Female', 'A+', 'City 2', NULL, NULL, NULL),
    ('admin1@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'Admin 1', 'ADMIN', 'super1@fitnexus.test', NULL, 'Head Office 1', 'Branch 1', 'Department 1', 'Team 1', 'Designation 1', 'Admin 1', '', '+912000000001', 'EMP001', 'UG', NULL, NULL, NULL, NULL, 'Female', 'B+', 'City 1', 'Address 1', NULL, NULL),
    ('admin2@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'Admin 2', 'ADMIN', 'super1@fitnexus.test', NULL, 'Head Office 2', 'Branch 3', 'Department 3', 'Team 3', 'Designation 4', 'Admin 2', '', '+912000000002', 'EMP002', 'PG', NULL, NULL, NULL, NULL, 'Male', 'AB+', 'City 2', 'Address 2', NULL, NULL),
    ('manager1@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'Manager 1', 'MANAGER', 'admin1@fitnexus.test', NULL, 'Head Office 1', 'Branch 1', 'Department 2', 'Team 2', 'Designation 2', 'Manager 1', '', '+913000000001', 'EMP003', 'PG', NULL, NULL, NULL, NULL, 'Female', 'O-', 'City 3', 'Address 3', NULL, NULL),
    ('manager2@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'Manager 2', 'MANAGER', 'admin1@fitnexus.test', NULL, 'Head Office 1', 'Branch 2', NULL, NULL, NULL, 'Manager 2', '', '+913000000002', 'EMP004', 'UG', NULL, NULL, NULL, NULL, 'Male', 'A-', 'City 4', 'Address 4', NULL, NULL),
    ('trainer1@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'Trainer 1', 'TRAINER', 'manager1@fitnexus.test', 'manager1@fitnexus.test', 'Head Office 1', 'Branch 1', 'Department 2', 'Team 2', 'Designation 3', 'Trainer 1', '', '+914000000001', 'EMP005', 'Diploma', 'Specialization 1', 1, 'Certification 1', 500, 'Male', 'B-', 'City 5', 'Address 5', NULL, NULL),
    ('trainer2@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'Trainer 2', 'TRAINER', 'manager1@fitnexus.test', 'manager1@fitnexus.test', 'Head Office 3', 'Branch 4', 'Department 4', 'Team 4', NULL, 'Trainer 2', '', '+914000000002', 'EMP006', 'UG', 'Specialization 2', 2, 'Certification 2', 600, 'Female', 'O+', 'City 6', 'Address 6', NULL, NULL),
    ('trainer3@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'Trainer 3', 'TRAINER', 'manager1@fitnexus.test', 'manager1@fitnexus.test', 'Head Office 2', 'Branch 3', 'Department 3', 'Team 3', NULL, 'Trainer 3', '', '+914000000003', 'EMP007', 'Professional Certification', 'Specialization 3', 3, 'Certification 3', 700, 'Male', 'A+', 'City 7', 'Address 7', NULL, NULL),
    ('user1@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'User 1', 'USER', 'trainer1@fitnexus.test', 'trainer1@fitnexus.test', 'Head Office 1', 'Branch 1', NULL, NULL, NULL, 'User 1', '', '+915000000001', NULL, NULL, NULL, NULL, NULL, NULL, 'Female', 'B+', 'City 8', 'Address 8', 'Goal 1', 'Diet 1'),
    ('user2@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'User 2', 'USER', 'trainer1@fitnexus.test', 'trainer1@fitnexus.test', 'Head Office 1', 'Branch 1', NULL, NULL, NULL, 'User 2', '', '+915000000002', NULL, NULL, NULL, NULL, NULL, NULL, 'Male', 'O+', 'City 9', 'Address 9', 'Goal 2', 'Diet 2'),
    ('user3@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'User 3', 'USER', 'trainer2@fitnexus.test', 'trainer2@fitnexus.test', 'Head Office 3', 'Branch 4', NULL, NULL, NULL, 'User 3', '', '+915000000003', NULL, NULL, NULL, NULL, NULL, NULL, 'Female', 'AB-', 'City 10', 'Address 10', 'Goal 3', 'Diet 3'),
    ('user4@fitnexus.test', '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e', 'User 4', 'USER', 'trainer3@fitnexus.test', 'trainer3@fitnexus.test', 'Head Office 2', 'Branch 3', NULL, NULL, NULL, 'User 4', '', '+915000000004', NULL, NULL, NULL, NULL, NULL, NULL, 'Male', 'A-', 'City 11', 'Address 11', 'Goal 4', 'Diet 4');

INSERT INTO user_accounts (
    email, password, name, role, is_active, is_approved, created_by_id, reports_to_id,
    created_at, updated_at, head_office_id, branch_id, department_id, team_id, designation_id
)
SELECT p.email, p.password, p.name, p.role, TRUE, TRUE, creator.id, reporter.id,
    NOW(), NOW(), ho.id, b.id, d.id, t.id, des.id
FROM seed_people p
LEFT JOIN user_accounts creator ON creator.email = p.creator_email
LEFT JOIN user_accounts reporter ON reporter.email = p.reports_to_email
LEFT JOIN head_offices ho ON ho.name = p.head_office_name
LEFT JOIN branches b ON b.name = p.branch_name
LEFT JOIN departments d ON d.name = p.department_name AND (b.id IS NULL OR d.branch_id = b.id)
LEFT JOIN teams t ON t.name = p.team_name AND (d.id IS NULL OR t.department_id = d.id)
LEFT JOIN designations des ON des.name = p.designation_name AND (d.id IS NULL OR des.department_id = d.id)
ON CONFLICT (email) DO UPDATE SET
    password = EXCLUDED.password,
    name = EXCLUDED.name,
    role = EXCLUDED.role,
    is_active = TRUE,
    is_approved = TRUE,
    created_by_id = EXCLUDED.created_by_id,
    reports_to_id = EXCLUDED.reports_to_id,
    head_office_id = EXCLUDED.head_office_id,
    branch_id = EXCLUDED.branch_id,
    department_id = EXCLUDED.department_id,
    team_id = EXCLUDED.team_id,
    designation_id = EXCLUDED.designation_id,
    updated_at = NOW();

INSERT INTO super_admins (user_id, first_name, last_name, join_date)
SELECT ua.id, p.first_name, COALESCE(p.last_name, ''), NOW() - INTERVAL '90 days'
FROM seed_people p JOIN user_accounts ua ON ua.email = p.email
WHERE p.role = 'SUPER_ADMIN'
ON CONFLICT (user_id) DO UPDATE SET first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name;

INSERT INTO admins (
    user_id, first_name, last_name, department, phone, employee_id, qualification, join_date,
    date_of_birth, gender, blood_group, personal_email, alternate_phone, emergency_contact,
    emergency_phone, current_address, permanent_address, city, state, pincode, employment_type,
    work_location, father_name, mother_name, location, emergency_contact_relationship,
    emergency_contact_name_2, emergency_contact_relationship_2, emergency_phone_2,
    reference_name_1, reference_phone_1, reference_name_2, reference_phone_2,
    joining_branch_name, source_platform, declaration_date, declaration_place
)
SELECT ua.id, p.first_name, p.last_name, COALESCE(p.department_name, 'Department 1'), p.phone, p.employee_id, p.qualification, NOW() - INTERVAL '60 days',
    DATE '1992-01-01', p.gender, p.blood_group, p.email, '+916000000001', 'Emergency Name 1',
    '+916000000002', p.address, p.address, p.city, 'State 1', '600001', 'Full-time',
    'Branch-based', 'Father 1', 'Mother 1', p.city, 'Emergency Relationship 1',
    'Emergency Name 2', 'Emergency Relationship 2', '+916000000003',
    'Reference 1', '+916000000004', 'Reference 2', '+916000000005',
    p.branch_name, 'Direct Interview', CURRENT_DATE, p.city
FROM seed_people p JOIN user_accounts ua ON ua.email = p.email
WHERE p.role = 'ADMIN'
ON CONFLICT (user_id) DO UPDATE SET
    first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name, department = EXCLUDED.department,
    phone = EXCLUDED.phone, employee_id = EXCLUDED.employee_id, qualification = EXCLUDED.qualification,
    current_address = EXCLUDED.current_address, city = EXCLUDED.city;

INSERT INTO managers (
    user_id, first_name, last_name, department, phone, employee_id, qualification, join_date,
    date_of_birth, gender, blood_group, personal_email, alternate_phone, emergency_contact,
    emergency_phone, current_address, permanent_address, city, state, pincode, employment_type,
    work_location, father_name, mother_name, location, emergency_contact_relationship,
    emergency_contact_name_2, emergency_contact_relationship_2, emergency_phone_2,
    reference_name_1, reference_phone_1, reference_name_2, reference_phone_2,
    joining_branch_name, source_platform, declaration_date, declaration_place
)
SELECT ua.id, p.first_name, p.last_name, COALESCE(p.department_name, 'Department 1'), p.phone, p.employee_id, p.qualification, NOW() - INTERVAL '45 days',
    DATE '1990-01-01', p.gender, p.blood_group, p.email, '+916100000001', 'Emergency Name 1',
    '+916100000002', p.address, p.address, p.city, 'State 1', '600002', 'Full-time',
    'Branch-based', 'Father 1', 'Mother 1', p.city, 'Emergency Relationship 1',
    'Emergency Name 2', 'Emergency Relationship 2', '+916100000003',
    'Reference 1', '+916100000004', 'Reference 2', '+916100000005',
    p.branch_name, 'Friend Refer', CURRENT_DATE, p.city
FROM seed_people p JOIN user_accounts ua ON ua.email = p.email
WHERE p.role = 'MANAGER'
ON CONFLICT (user_id) DO UPDATE SET
    first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name, department = EXCLUDED.department,
    phone = EXCLUDED.phone, employee_id = EXCLUDED.employee_id, qualification = EXCLUDED.qualification,
    current_address = EXCLUDED.current_address, city = EXCLUDED.city;

INSERT INTO trainers (
    user_id, first_name, last_name, specialization, experience_years, certification,
    phone, qualification, rate_per_hour, bio, languages, rating, total_clients_trained,
    join_date, date_of_birth, gender, blood_group, personal_email, alternate_phone,
    emergency_contact, emergency_phone, current_address, permanent_address, city, state,
    pincode, employment_type, work_location, reporting_manager_name, father_name,
    mother_name, location, emergency_contact_relationship, emergency_contact_name_2,
    emergency_contact_relationship_2, emergency_phone_2, reference_name_1, reference_phone_1,
    reference_name_2, reference_phone_2, joining_branch_name, source_platform,
    declaration_date, declaration_place
)
SELECT ua.id, p.first_name, p.last_name, p.specialization, p.experience_years, p.certification,
    p.phone, p.qualification, p.rate_per_hour, 'Trainer bio', 'English, Tamil', 4.50, 10,
    NOW() - INTERVAL '30 days', DATE '1994-01-01', p.gender, p.blood_group, p.email, '+916200000001',
    'Emergency Name 1', '+916200000002', p.address, p.address, p.city, 'State 1',
    '600003', 'Full-time', 'Branch-based', 'Manager 1', 'Father 1',
    'Mother 1', p.city, 'Emergency Relationship 1', 'Emergency Name 2',
    'Emergency Relationship 2', '+916200000003', 'Reference 1', '+916200000004',
    'Reference 2', '+916200000005', p.branch_name, 'Job Fair', CURRENT_DATE, p.city
FROM seed_people p JOIN user_accounts ua ON ua.email = p.email
WHERE p.role = 'TRAINER'
ON CONFLICT (user_id) DO UPDATE SET
    first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name, specialization = EXCLUDED.specialization,
    experience_years = EXCLUDED.experience_years, certification = EXCLUDED.certification,
    phone = EXCLUDED.phone, qualification = EXCLUDED.qualification, rate_per_hour = EXCLUDED.rate_per_hour,
    current_address = EXCLUDED.current_address, city = EXCLUDED.city;

INSERT INTO users (
    user_id, first_name, last_name, weight, height, blood_group, age, gender, phone,
    address, city, medical_conditions, emergency_contact, emergency_phone,
    assigned_trainer_id, details_completed, registration_date, activation_date, bio,
    date_of_birth, fitness_goals, dietary_preferences, injuries_or_limitations
)
SELECT ua.id, p.first_name, p.last_name, 70.0, 170.0, p.blood_group, 25, p.gender, p.phone,
    p.address, p.city, 'None', 'Emergency Name 1', '+916300000001',
    trainer_profile.user_id, TRUE, NOW() - INTERVAL '15 days', NOW() - INTERVAL '10 days',
    'User bio', DATE '1996-01-01', p.fitness_goals, p.dietary_preferences, 'None'
FROM seed_people p
JOIN user_accounts ua ON ua.email = p.email
LEFT JOIN user_accounts trainer_account ON trainer_account.email = p.reports_to_email
LEFT JOIN trainers trainer_profile ON trainer_profile.user_id = trainer_account.id
WHERE p.role = 'USER'
ON CONFLICT (user_id) DO UPDATE SET
    first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name, phone = EXCLUDED.phone,
    address = EXCLUDED.address, city = EXCLUDED.city, assigned_trainer_id = EXCLUDED.assigned_trainer_id,
    details_completed = TRUE, fitness_goals = EXCLUDED.fitness_goals, dietary_preferences = EXCLUDED.dietary_preferences;

SELECT setval('user_accounts_id_seq', GREATEST(COALESCE((SELECT MAX(id) FROM user_accounts), 0), 1), TRUE);

COMMIT;