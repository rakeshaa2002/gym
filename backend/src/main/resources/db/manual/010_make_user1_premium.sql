-- Promote user1@fitnexus.test to the PREMIUM plan (unlimited anytime entry +
-- wellness chat). user1 is already assigned to Trainer 1 by 006_seed_dummy_data.sql.
--
-- Run AFTER restarting the updated backend (so the membership_plans table, the
-- PREMIUM plan, and the users.membership_plan_id column exist):
--   psql -U postgres -d Fitnexus -f src/main/resources/db/manual/010_make_user1_premium.sql
--
-- Login: user1@fitnexus.test / Admin@123

BEGIN;

UPDATE users u
SET membership_plan     = 'PREMIUM',
    membership_plan_id  = (SELECT id FROM membership_plans WHERE code = 'PREMIUM'),
    membership_expiry   = CURRENT_DATE + 365,
    access_start_time   = NULL,   -- Premium is unlimited; no time slot needed
    access_end_time     = NULL
FROM user_accounts ua
WHERE ua.id = u.user_id
  AND ua.email = 'user1@fitnexus.test';

COMMIT;

-- Verify:
--   SELECT ua.email, u.membership_plan, u.membership_plan_id, u.membership_expiry, u.assigned_trainer_id
--   FROM users u JOIN user_accounts ua ON ua.id = u.user_id
--   WHERE ua.email = 'user1@fitnexus.test';
