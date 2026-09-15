-- Seed test transactions for the Revenue Dashboard demo.
-- Run after 006_seed_dummy_data.sql to populate the revenue cards.
-- All passwords: Admin@123
-- Usage:
--   psql -U postgres -d fitnexus -f src/main/resources/db/manual/012_seed_test_transactions.sql

BEGIN;

-- Insert some SUCCESS transactions for today and this month
INSERT INTO transactions (member_id, plan_id, amount, months, payment_method, status, transaction_date)
SELECT ua.id, 3, 999, 1, 'CASH', 'SUCCESS', NOW()
FROM user_accounts ua WHERE ua.email = 'user1@fitnexus.test'
AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.member_id = ua.id AND t.transaction_date::date = CURRENT_DATE AND t.status = 'SUCCESS')
;

INSERT INTO transactions (member_id, plan_id, amount, months, payment_method, status, transaction_date)
SELECT ua.id, 2, 599, 1, 'RAZORPAY', 'SUCCESS', NOW()
FROM user_accounts ua WHERE ua.email = 'user2@fitnexus.test'
AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.member_id = ua.id AND t.transaction_date::date = CURRENT_DATE AND t.status = 'SUCCESS')
;

INSERT INTO transactions (member_id, plan_id, amount, months, payment_method, status, transaction_date)
SELECT ua.id, 1, 299, 1, 'CASH', 'SUCCESS', NOW() - INTERVAL '5 days'
FROM user_accounts ua WHERE ua.email = 'user3@fitnexus.test'
AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.member_id = ua.id AND DATE_PART('month', t.transaction_date) = DATE_PART('month', NOW()) AND t.status = 'SUCCESS')
;

INSERT INTO transactions (member_id, plan_id, amount, months, payment_method, status, transaction_date)
SELECT ua.id, 3, 999, 1, 'CARD', 'SUCCESS', NOW() - INTERVAL '10 days'
FROM user_accounts ua WHERE ua.email = 'user4@fitnexus.test'
AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.member_id = ua.id AND DATE_PART('month', t.transaction_date) = DATE_PART('month', NOW()) AND t.status = 'SUCCESS')
;

-- Insert a PENDING (outstanding dues) transaction
INSERT INTO transactions (member_id, plan_id, amount, months, payment_method, status, transaction_date)
SELECT ua.id, 2, 599, 1, 'BANK_TRANSFER', 'PENDING', NOW() - INTERVAL '2 days'
FROM user_accounts ua WHERE ua.email = 'user1@fitnexus.test'
AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.member_id = ua.id AND t.status = 'PENDING')
;

COMMIT;
