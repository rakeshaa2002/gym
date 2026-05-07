-- Manual PostgreSQL migration for replacing the old single app_users table
-- with a small user_accounts identity table plus separate role profile tables.
--
-- Run this once before starting the refactored backend against an existing DB:
--   psql -U postgres -d fitnexus -f src/main/resources/db/manual/001_split_app_users_into_role_tables.sql

BEGIN;

CREATE TABLE IF NOT EXISTS user_accounts (
    id BIGINT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_id BIGINT NULL REFERENCES user_accounts(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMP NULL,
    head_office_id BIGINT NULL,
    branch_id BIGINT NULL,
    department_id BIGINT NULL,
    team_id BIGINT NULL,
    designation_id BIGINT NULL
);

CREATE SEQUENCE IF NOT EXISTS user_accounts_id_seq OWNED BY user_accounts.id;
ALTER TABLE user_accounts ALTER COLUMN id SET DEFAULT nextval('user_accounts_id_seq');

INSERT INTO user_accounts (
    id, email, password, name, role, is_active, is_approved, created_by_id,
    created_at, updated_at, last_login_at, head_office_id, branch_id,
    department_id, team_id, designation_id
)
SELECT
    id, email, password, name, role, is_active, is_approved, created_by_id,
    created_at, updated_at, last_login_at, head_office_id, branch_id,
    department_id, team_id, designation_id
FROM app_users
ON CONFLICT (id) DO NOTHING;

SELECT setval(
    'user_accounts_id_seq',
    GREATEST(COALESCE((SELECT MAX(id) FROM user_accounts), 0), 1),
    TRUE
);

CREATE TABLE IF NOT EXISTS super_admins (
    user_id BIGINT PRIMARY KEY REFERENCES user_accounts(id) ON DELETE CASCADE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    join_date TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS admins (
    user_id BIGINT PRIMARY KEY REFERENCES user_accounts(id) ON DELETE CASCADE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    qualification VARCHAR(100) NOT NULL,
    join_date TIMESTAMP NULL,
    bio VARCHAR(500) NULL
);

CREATE TABLE IF NOT EXISTS managers (
    user_id BIGINT PRIMARY KEY REFERENCES user_accounts(id) ON DELETE CASCADE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    qualification VARCHAR(100) NOT NULL,
    join_date TIMESTAMP NULL,
    bio VARCHAR(500) NULL
);

CREATE TABLE IF NOT EXISTS trainers (
    user_id BIGINT PRIMARY KEY REFERENCES user_accounts(id) ON DELETE CASCADE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    experience_years INTEGER NOT NULL,
    certification VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    qualification VARCHAR(100) NOT NULL,
    rate_per_hour DOUBLE PRECISION NOT NULL,
    bio VARCHAR(500) NULL,
    languages VARCHAR(200) NULL,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_clients_trained INTEGER DEFAULT 0,
    join_date TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY REFERENCES user_accounts(id) ON DELETE CASCADE,
    first_name VARCHAR(50) NULL,
    last_name VARCHAR(50) NULL,
    weight DOUBLE PRECISION NULL,
    height DOUBLE PRECISION NULL,
    blood_group VARCHAR(3) NULL,
    age INTEGER NULL,
    gender VARCHAR(20) NULL,
    phone VARCHAR(15) NULL,
    address VARCHAR(200) NULL,
    city VARCHAR(50) NULL,
    medical_conditions VARCHAR(500) NULL,
    emergency_contact VARCHAR(100) NULL,
    emergency_phone VARCHAR(15) NULL,
    assigned_trainer_id BIGINT NULL REFERENCES trainers(user_id),
    details_completed BOOLEAN NOT NULL DEFAULT FALSE,
    registration_date TIMESTAMP NULL,
    activation_date TIMESTAMP NULL,
    bio VARCHAR(500) NULL,
    date_of_birth TIMESTAMP NULL,
    fitness_goals VARCHAR(300) NULL,
    dietary_preferences VARCHAR(300) NULL,
    injuries_or_limitations VARCHAR(500) NULL
);

INSERT INTO super_admins (user_id, first_name, last_name, join_date)
SELECT id, COALESCE(first_name, split_part(name, ' ', 1), 'Super'), COALESCE(NULLIF(last_name, ''), 'Admin'), join_date
FROM app_users
WHERE role = 'SUPER_ADMIN'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO admins (user_id, first_name, last_name, department, phone, employee_id, qualification, join_date, bio)
SELECT
    id,
    COALESCE(first_name, split_part(name, ' ', 1), 'Admin'),
    COALESCE(NULLIF(last_name, ''), 'User'),
    COALESCE(NULLIF(department, ''), 'Administration'),
    COALESCE(NULLIF(phone, ''), '0000000000'),
    COALESCE(NULLIF(employee_id, ''), 'ADM' || id),
    COALESCE(NULLIF(qualification, ''), 'N/A'),
    join_date,
    bio
FROM app_users
WHERE role = 'ADMIN'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO managers (user_id, first_name, last_name, department, phone, employee_id, qualification, join_date, bio)
SELECT
    id,
    COALESCE(first_name, split_part(name, ' ', 1), 'Manager'),
    COALESCE(NULLIF(last_name, ''), 'User'),
    COALESCE(NULLIF(department, ''), 'Management'),
    COALESCE(NULLIF(phone, ''), '0000000000'),
    COALESCE(NULLIF(employee_id, ''), 'MGR' || id),
    COALESCE(NULLIF(qualification, ''), 'N/A'),
    join_date,
    bio
FROM app_users
WHERE role = 'MANAGER'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO trainers (
    user_id, first_name, last_name, specialization, experience_years, certification,
    phone, qualification, rate_per_hour, bio, languages, rating, total_clients_trained, join_date
)
SELECT
    id,
    COALESCE(first_name, split_part(name, ' ', 1), 'Trainer'),
    COALESCE(NULLIF(last_name, ''), 'User'),
    COALESCE(NULLIF(specialization, ''), 'General Training'),
    COALESCE(experience_years, 0),
    COALESCE(NULLIF(certification, ''), 'N/A'),
    COALESCE(NULLIF(phone, ''), '0000000000'),
    COALESCE(NULLIF(qualification, ''), 'N/A'),
    COALESCE(rate_per_hour, 100.0),
    bio,
    languages,
    COALESCE(rating, 0.00),
    COALESCE(total_clients_trained, 0),
    join_date
FROM app_users
WHERE role = 'TRAINER'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users (
    user_id, first_name, last_name, weight, height, blood_group, age, gender,
    phone, address, city, medical_conditions, emergency_contact, emergency_phone,
    assigned_trainer_id, details_completed, registration_date, activation_date, bio,
    date_of_birth, fitness_goals, dietary_preferences, injuries_or_limitations
)
SELECT
    id,
    first_name,
    last_name,
    weight,
    height,
    blood_group,
    age,
    gender,
    phone,
    address,
    city,
    medical_conditions,
    emergency_contact,
    emergency_phone,
    assigned_trainer_id,
    COALESCE(details_completed, FALSE),
    registration_date,
    activation_date,
    bio,
    date_of_birth,
    fitness_goals,
    dietary_preferences,
    injuries_or_limitations
FROM app_users
WHERE role = 'USER'
ON CONFLICT (user_id) DO NOTHING;

COMMIT;
