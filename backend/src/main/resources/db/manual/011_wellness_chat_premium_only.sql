-- Make wellness (trainer) chat a PREMIUM-only perk.
--
-- The MembershipPlanBootstrapper only backfills trainer_chat when it is NULL, so
-- an already-seeded Standard plan keeps its old trainer_chat = true. This migration
-- revokes it from Standard and updates its feature bullets to match. Premium keeps
-- trainer chat as its exclusive perk.
--
-- Run AFTER restarting the updated backend:
--   psql -U postgres -d Fitnexus -f src/main/resources/db/manual/011_wellness_chat_premium_only.sql

BEGIN;

-- Standard (and anything below Premium that isn't unlimited) no longer gets chat.
UPDATE membership_plans
SET trainer_chat = FALSE
WHERE UPPER(code) <> 'PREMIUM'
  AND COALESCE(unlimited_access, FALSE) = FALSE;

-- Refresh Standard's marketing copy so the card no longer advertises chat.
UPDATE membership_plans
SET description = 'Wider assigned time slot — up to 2 hours per visit, with personalised plans.',
    features = 'Everything in Basic' || chr(10) ||
               '2 hours per visit' || chr(10) ||
               'Personalised workout & diet plans' || chr(10) ||
               'Detailed progress analytics'
WHERE UPPER(code) = 'STANDARD';

-- Premium remains the only tier with wellness chat.
UPDATE membership_plans
SET trainer_chat = TRUE
WHERE UPPER(code) = 'PREMIUM';

COMMIT;

-- Verify:
--   SELECT code, trainer_chat, unlimited_access FROM membership_plans ORDER BY price;
