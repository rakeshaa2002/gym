-- Update test users with membership expiry dates close to today
-- so the Renewal Management dashboard shows real data.
-- Run this after 006_seed_dummy_data.sql

BEGIN;

-- user1 expires in 3 days (critical)
UPDATE users u
SET membership_expiry = CURRENT_DATE + INTERVAL '3 days'
FROM user_accounts ua
WHERE ua.id = u.user_id AND ua.email = 'user1@fitnexus.test';

-- user2 expires in 7 days (warning)
UPDATE users u
SET membership_expiry = CURRENT_DATE + INTERVAL '7 days'
FROM user_accounts ua
WHERE ua.id = u.user_id AND ua.email = 'user2@fitnexus.test';

-- user3 expires in 15 days (info)
UPDATE users u
SET membership_expiry = CURRENT_DATE + INTERVAL '15 days'
FROM user_accounts ua
WHERE ua.id = u.user_id AND ua.email = 'user3@fitnexus.test';

-- user4 expires in 28 days (success)
UPDATE users u
SET membership_expiry = CURRENT_DATE + INTERVAL '28 days'
FROM user_accounts ua
WHERE ua.id = u.user_id AND ua.email = 'user4@fitnexus.test';

-- Rakesha expires tomorrow (most urgent)
UPDATE users u
SET membership_expiry = CURRENT_DATE + INTERVAL '1 day'
FROM user_accounts ua
WHERE ua.id = u.user_id AND ua.email = 'rakeshan099@gmail.com';

COMMIT;
