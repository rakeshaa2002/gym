-- Test data: one PREMIUM member + their personal trainer, so you can try
-- the wellness chat and unlimited (anytime) attendance.
--
-- Run AFTER starting the updated backend at least once (so the membership_plans
-- table + the new users columns exist and the default plans are seeded):
--   psql -U postgres -d Fitnexus -f src/main/resources/db/manual/009_seed_premium_member_chat.sql
--
-- Logins (password for both: Admin@123)
--   Member : premium.member@fitnexus.test   (Premium plan, unlimited entry)
--   Trainer: coach.maya@fitnexus.test       (their personal trainer)

BEGIN;

-- 1) Trainer account + trainer profile -----------------------------------------
INSERT INTO user_accounts (email, password, name, role, is_active, is_approved, created_at, updated_at)
VALUES ('coach.maya@fitnexus.test',
        '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e',
        'Coach Maya', 'TRAINER', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET role = 'TRAINER', is_active = TRUE, is_approved = TRUE, updated_at = NOW();

INSERT INTO trainers (user_id, first_name, last_name, specialization, experience_years,
                      certification, phone, qualification, rate_per_hour, join_date)
SELECT id, 'Maya', 'Coach', 'Strength & Conditioning', 5, 'NASM-CPT',
       '+919900000001', 'BSc Sports Science', 800, NOW()
FROM user_accounts WHERE email = 'coach.maya@fitnexus.test'
ON CONFLICT (user_id) DO NOTHING;

-- 2) Member account ------------------------------------------------------------
INSERT INTO user_accounts (email, password, name, role, is_active, is_approved, created_at, updated_at)
VALUES ('premium.member@fitnexus.test',
        '$2a$10$Muu/IT4JynSr4hW/gurLpO64SnfIAjlmAusQCrWD7DStbjgtEl17e',
        'Premium Member', 'USER', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET role = 'USER', is_active = TRUE, is_approved = TRUE, updated_at = NOW();

-- Link the member's account to the trainer (fallback path) ----------------------
UPDATE user_accounts m
SET trainer_id = t.id, updated_at = NOW()
FROM user_accounts t
WHERE m.email = 'premium.member@fitnexus.test' AND t.email = 'coach.maya@fitnexus.test';

-- 3) Member fitness profile: PREMIUM plan + assigned trainer --------------------
--    Premium => unlimited anytime entry (no time slot) + wellness chat enabled.
INSERT INTO users (user_id, first_name, last_name, blood_group, gender, phone,
                   details_completed, registration_date, activation_date,
                   membership_plan, membership_plan_id, membership_expiry, assigned_trainer_id)
SELECT m.id, 'Premium', 'Member', 'O+', 'Female', '+919900000002',
       TRUE, NOW(), NOW(),
       'PREMIUM', (SELECT id FROM membership_plans WHERE code = 'PREMIUM'),
       CURRENT_DATE + 365, t.id
FROM user_accounts m
JOIN user_accounts t ON t.email = 'coach.maya@fitnexus.test'
WHERE m.email = 'premium.member@fitnexus.test'
ON CONFLICT (user_id) DO UPDATE SET
    membership_plan = 'PREMIUM',
    membership_plan_id = (SELECT id FROM membership_plans WHERE code = 'PREMIUM'),
    membership_expiry = CURRENT_DATE + 365,
    assigned_trainer_id = EXCLUDED.assigned_trainer_id,
    access_start_time = NULL,
    access_end_time = NULL,
    details_completed = TRUE;

COMMIT;
