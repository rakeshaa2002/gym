-- Manual PostgreSQL seed data: Workout catalog, Diet plans, and Schedules.
-- These are the modules NOT covered by 006_seed_dummy_data.sql (which seeds the
-- organization tree + accounts). Run AFTER 006 so the trainer/user accounts and
-- org tree already exist.
--
--   psql -U postgres -d Fitnexus -f src/main/resources/db/manual/008_seed_workout_diet_schedules.sql
--
-- Idempotent: re-running updates/skips existing rows instead of duplicating.
-- Schedules link to the demo accounts seeded in 006 (trainer1/2@fitnexus.test,
-- user1..4@fitnexus.test).

BEGIN;

-- =============================================================================
-- 1. WORKOUT TYPES  (name is UNIQUE -> upsert)
-- =============================================================================
INSERT INTO workout_types (name, description, status, created_at, updated_at)
VALUES
    ('Strength',     'Resistance training to build muscle and raw strength.',        'ACTIVE', NOW(), NOW()),
    ('Cardio',       'Aerobic conditioning for heart health and fat loss.',          'ACTIVE', NOW(), NOW()),
    ('HIIT',         'High-intensity interval training, short explosive sessions.',  'ACTIVE', NOW(), NOW()),
    ('Yoga',         'Mobility, flexibility, and breathing-focused practice.',       'ACTIVE', NOW(), NOW()),
    ('Functional',   'Compound, real-world movement patterns and core stability.',   'ACTIVE', NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    status      = EXCLUDED.status,
    updated_at  = NOW();

-- =============================================================================
-- 2. BODY PARTS  (name is UNIQUE -> upsert)
-- =============================================================================
INSERT INTO body_parts (name, description, status, created_at, updated_at)
VALUES
    ('Chest',     'Pectoral muscles.',                'ACTIVE', NOW(), NOW()),
    ('Back',      'Lats, traps, and lower back.',     'ACTIVE', NOW(), NOW()),
    ('Legs',      'Quads, hamstrings, and calves.',   'ACTIVE', NOW(), NOW()),
    ('Shoulders', 'Deltoids and rotator cuff.',       'ACTIVE', NOW(), NOW()),
    ('Arms',      'Biceps and triceps.',              'ACTIVE', NOW(), NOW()),
    ('Core',      'Abdominals and obliques.',         'ACTIVE', NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    status      = EXCLUDED.status,
    updated_at  = NOW();

-- =============================================================================
-- 3. EXERCISES  (name not unique -> guard with NOT EXISTS)
--    FK columns resolved from the master tables above by name.
-- =============================================================================
INSERT INTO exercises (name, description, workout_type_id, body_part_id, difficulty,
                       equipment, calories_burned, sets, reps, duration_minutes,
                       status, created_at, updated_at)
SELECT v.name, v.description,
       (SELECT id FROM workout_types WHERE name = v.wtype),
       (SELECT id FROM body_parts    WHERE name = v.bpart),
       v.difficulty, v.equipment, v.calories, v.sets, v.reps, v.mins,
       'ACTIVE', NOW(), NOW()
FROM (VALUES
    ('Barbell Bench Press', 'Compound horizontal press for the chest.',     'Strength',   'Chest',     'INTERMEDIATE', 'Barbell, Bench', 180, 4, 10, 30),
    ('Push Up',             'Bodyweight chest and triceps press.',          'Functional', 'Chest',     'BEGINNER',     'None',           90,  3, 15, 12),
    ('Deadlift',            'Hip-hinge pull working the full posterior chain.','Strength', 'Back',      'ADVANCED',     'Barbell',        220, 4, 6,  35),
    ('Lat Pulldown',        'Vertical pull for lat width.',                 'Strength',   'Back',      'BEGINNER',     'Cable Machine',  120, 3, 12, 20),
    ('Barbell Squat',       'Compound lower-body strength builder.',        'Strength',   'Legs',      'INTERMEDIATE', 'Barbell, Rack',  200, 4, 8,  30),
    ('Walking Lunge',       'Unilateral leg and glute movement.',           'Functional', 'Legs',      'BEGINNER',     'Dumbbells',      130, 3, 12, 15),
    ('Overhead Press',      'Standing shoulder press.',                     'Strength',   'Shoulders', 'INTERMEDIATE', 'Barbell',        140, 4, 8,  20),
    ('Bicep Curl',          'Isolation curl for the biceps.',               'Strength',   'Arms',      'BEGINNER',     'Dumbbells',      70,  3, 12, 12),
    ('Plank',               'Isometric core hold.',                         'Functional', 'Core',      'BEGINNER',     'None',           50,  3, 1,  6),
    ('Burpee',              'Full-body explosive conditioning movement.',   'HIIT',       'Core',      'ADVANCED',     'None',           160, 4, 15, 18)
) AS v(name, description, wtype, bpart, difficulty, equipment, calories, sets, reps, mins)
WHERE NOT EXISTS (SELECT 1 FROM exercises e WHERE e.name = v.name);

-- Instructions (element collection: exercise_instructions[exercise_id, instruction])
INSERT INTO exercise_instructions (exercise_id, instruction)
SELECT e.id, v.instruction
FROM (VALUES
    ('Barbell Bench Press', 'Lie flat, grip slightly wider than shoulders.'),
    ('Barbell Bench Press', 'Lower the bar to mid-chest, then press up.'),
    ('Barbell Squat',       'Brace your core and keep your chest up.'),
    ('Barbell Squat',       'Descend until thighs are parallel, then drive up.'),
    ('Plank',               'Keep a straight line from head to heels.'),
    ('Plank',               'Hold for the prescribed time without sagging hips.'),
    ('Burpee',              'Drop to a push-up, jump feet in, then jump up.')
) AS v(exercise_name, instruction)
JOIN exercises e ON e.name = v.exercise_name
WHERE NOT EXISTS (
    SELECT 1 FROM exercise_instructions ei
    WHERE ei.exercise_id = e.id AND ei.instruction = v.instruction
);

-- =============================================================================
-- 4. WORKOUT PLANS  (name is UNIQUE -> upsert)
-- =============================================================================
INSERT INTO workout_plans (name, description, goal, level, duration_weeks, days_per_week,
                           estimated_time_minutes, status, notes, created_at, updated_at)
VALUES
    ('Beginner Full Body',   'A gentle 3-day full-body routine for new members.',          'General Fitness', 'BEGINNER',     4,  3, 45, 'ACTIVE', 'Focus on form over weight.', NOW(), NOW()),
    ('Strength Builder',     'Progressive overload program for raw strength.',             'Build Strength',  'INTERMEDIATE', 8,  4, 60, 'ACTIVE', 'Add weight each week.',      NOW(), NOW()),
    ('Fat Loss HIIT',        'High-intensity circuit plan for fat loss.',                  'Weight Loss',     'INTERMEDIATE', 6,  5, 30, 'ACTIVE', 'Keep rest periods short.',   NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET
    description             = EXCLUDED.description,
    goal                   = EXCLUDED.goal,
    level                  = EXCLUDED.level,
    duration_weeks         = EXCLUDED.duration_weeks,
    days_per_week          = EXCLUDED.days_per_week,
    estimated_time_minutes = EXCLUDED.estimated_time_minutes,
    status                 = EXCLUDED.status,
    updated_at             = NOW();

-- Plan <-> Exercise links (join table: workout_plan_exercises)
INSERT INTO workout_plan_exercises (workout_plan_id, exercise_id)
SELECT wp.id, e.id
FROM (VALUES
    ('Beginner Full Body', 'Push Up'),
    ('Beginner Full Body', 'Walking Lunge'),
    ('Beginner Full Body', 'Plank'),
    ('Beginner Full Body', 'Lat Pulldown'),
    ('Strength Builder',   'Barbell Bench Press'),
    ('Strength Builder',   'Deadlift'),
    ('Strength Builder',   'Barbell Squat'),
    ('Strength Builder',   'Overhead Press'),
    ('Fat Loss HIIT',      'Burpee'),
    ('Fat Loss HIIT',      'Walking Lunge'),
    ('Fat Loss HIIT',      'Plank')
) AS v(plan_name, exercise_name)
JOIN workout_plans wp ON wp.name = v.plan_name
JOIN exercises     e  ON e.name  = v.exercise_name
WHERE NOT EXISTS (
    SELECT 1 FROM workout_plan_exercises wpe
    WHERE wpe.workout_plan_id = wp.id AND wpe.exercise_id = e.id
);

-- =============================================================================
-- 5. DIET PLANS  (name not unique -> guard with NOT EXISTS)
-- =============================================================================
INSERT INTO diet_plans (name, description, eat_time, prep_time, cook_time, difficulty,
                        total_steps, health_score, calories, protein, carbs, fats,
                        cholesterol, sodium, potassium, vitamina, vitaminc, calcium, iron,
                        status, created_at, updated_at)
SELECT v.name, v.description, v.eat_time, v.prep, v.cook, v.difficulty,
       v.steps, v.health, v.calories, v.protein, v.carbs, v.fats,
       v.cholesterol, v.sodium, v.potassium, v.vit_a, v.vit_c, v.calcium, v.iron,
       'ACTIVE', NOW(), NOW()
FROM (VALUES
    ('High Protein Breakfast', 'Egg and oats breakfast to start the day strong.', 'Morning',  10, 10, 'EASY',   4, 85, 420, 32, 38, 14,  210, 320, 410, 12, 8,  15, 18),
    ('Lean Lunch Bowl',        'Grilled chicken, quinoa, and greens.',            'Afternoon',15, 20, 'MEDIUM', 6, 90, 540, 45, 50, 16,  120, 480, 620, 30, 45, 10, 22),
    ('Post-Workout Shake',     'Whey and banana recovery shake.',                 'Anytime',  5,  0,  'EASY',   2, 80, 280, 30, 35, 4,   40,  120, 520, 5,  12, 25, 6),
    ('Light Dinner',           'Baked fish with steamed vegetables.',             'Evening',  15, 25, 'MEDIUM', 5, 92, 380, 38, 22, 12,  90,  300, 540, 60, 35, 8,  14)
) AS v(name, description, eat_time, prep, cook, difficulty, steps, health, calories,
        protein, carbs, fats, cholesterol, sodium, potassium, vit_a, vit_c, calcium, iron)
WHERE NOT EXISTS (SELECT 1 FROM diet_plans dp WHERE dp.name = v.name);

-- Ingredients (element collection: diet_plan_ingredients[diet_plan_id, ingredient])
INSERT INTO diet_plan_ingredients (diet_plan_id, ingredient)
SELECT dp.id, v.ingredient
FROM (VALUES
    ('High Protein Breakfast', '3 eggs'),
    ('High Protein Breakfast', '1 cup rolled oats'),
    ('High Protein Breakfast', '1 tbsp honey'),
    ('Lean Lunch Bowl',        '150g grilled chicken breast'),
    ('Lean Lunch Bowl',        '1 cup cooked quinoa'),
    ('Lean Lunch Bowl',        'Mixed greens and olive oil'),
    ('Post-Workout Shake',     '1 scoop whey protein'),
    ('Post-Workout Shake',     '1 banana'),
    ('Light Dinner',           '180g white fish fillet'),
    ('Light Dinner',           'Steamed broccoli and carrots')
) AS v(plan_name, ingredient)
JOIN diet_plans dp ON dp.name = v.plan_name
WHERE NOT EXISTS (
    SELECT 1 FROM diet_plan_ingredients dpi
    WHERE dpi.diet_plan_id = dp.id AND dpi.ingredient = v.ingredient
);

-- Directions (element collection: diet_plan_directions[diet_plan_id, direction])
INSERT INTO diet_plan_directions (diet_plan_id, direction)
SELECT dp.id, v.direction
FROM (VALUES
    ('High Protein Breakfast', 'Cook the oats with water or milk.'),
    ('High Protein Breakfast', 'Scramble the eggs and serve with honey.'),
    ('Lean Lunch Bowl',        'Grill the chicken and slice.'),
    ('Lean Lunch Bowl',        'Combine quinoa, greens, and chicken; drizzle oil.'),
    ('Post-Workout Shake',     'Blend all ingredients until smooth.'),
    ('Light Dinner',           'Bake the fish at 200C for 15 minutes.'),
    ('Light Dinner',           'Steam the vegetables and plate together.')
) AS v(plan_name, direction)
JOIN diet_plans dp ON dp.name = v.plan_name
WHERE NOT EXISTS (
    SELECT 1 FROM diet_plan_directions dpd
    WHERE dpd.diet_plan_id = dp.id AND dpd.direction = v.direction
);

-- =============================================================================
-- 6. TRAINER DUTY SCHEDULES  (trainer shifts)
--    trainer_id / assigned_by_id reference user_accounts by email.
-- =============================================================================
INSERT INTO trainer_duty_schedules (trainer_id, assigned_by_id, branch, title, description,
                                    start_date_time, end_date_time, repeat_type, shift_type,
                                    location, status, notes, created_at, updated_at)
SELECT
    (SELECT id FROM user_accounts WHERE email = v.trainer_email),
    (SELECT id FROM user_accounts WHERE email = v.assigner_email),
    v.branch, v.title, v.description,
    v.start_dt::timestamp, v.end_dt::timestamp, v.repeat_type, v.shift_type,
    v.location, 'ACTIVE', v.notes, NOW(), NOW()
FROM (VALUES
    ('trainer1@fitnexus.test', 'manager1@fitnexus.test', 'Branch 1', 'Morning Floor Duty',   'Supervise the gym floor and assist members.', '2026-06-16 06:00', '2026-06-16 10:00', 'WEEKLY', 'MORNING',   'Branch 1 - Main Floor', 'Weekday morning shift.'),
    ('trainer1@fitnexus.test', 'manager1@fitnexus.test', 'Branch 1', 'Evening PT Sessions',   'One-on-one personal training slots.',         '2026-06-16 17:00', '2026-06-16 21:00', 'WEEKLY', 'EVENING',   'Branch 1 - PT Studio',  'Booked client sessions.'),
    ('trainer2@fitnexus.test', 'manager1@fitnexus.test', 'Branch 4', 'Weekend Group Class',   'Lead the weekend HIIT group class.',          '2026-06-20 09:00', '2026-06-20 11:00', 'WEEKLY', 'MORNING',   'Branch 4 - Studio A',   'High attendance expected.')
) AS v(trainer_email, assigner_email, branch, title, description, start_dt, end_dt, repeat_type, shift_type, location, notes)
WHERE NOT EXISTS (
    SELECT 1 FROM trainer_duty_schedules t
    WHERE t.title = v.title
      AND t.trainer_id = (SELECT id FROM user_accounts WHERE email = v.trainer_email)
      AND t.start_date_time = v.start_dt::timestamp
);

-- =============================================================================
-- 7. USER WORKOUT SCHEDULES  (customer training sessions)
--    Links a trainer + user, optionally a workout plan and workout type.
-- =============================================================================
INSERT INTO user_workout_schedules (trainer_id, user_id, workout_plan_id, workout_type_id,
                                    title, description, start_date_time, end_date_time,
                                    repeat_type, location, completion_status, notes,
                                    status, created_at, updated_at)
SELECT
    (SELECT id FROM user_accounts WHERE email = v.trainer_email),
    (SELECT id FROM user_accounts WHERE email = v.user_email),
    (SELECT id FROM workout_plans WHERE name = v.plan_name),
    (SELECT id FROM workout_types WHERE name = v.wtype),
    v.title, v.description, v.start_dt::timestamp, v.end_dt::timestamp,
    v.repeat_type, v.location, v.completion, v.notes, 'ACTIVE', NOW(), NOW()
FROM (VALUES
    ('trainer1@fitnexus.test', 'user1@fitnexus.test', 'Beginner Full Body', 'Functional', 'Intro Full Body',   'First guided full-body session.',   '2026-06-16 07:00', '2026-06-16 08:00', 'WEEKLY', 'Branch 1 - Main Floor', 'PENDING',   'New member onboarding.'),
    ('trainer1@fitnexus.test', 'user2@fitnexus.test', 'Strength Builder',   'Strength',   'Strength Day - Push','Bench and overhead press focus.',   '2026-06-16 18:00', '2026-06-16 19:00', 'WEEKLY', 'Branch 1 - Weights',    'PENDING',   'Track top set weight.'),
    ('trainer1@fitnexus.test', 'user1@fitnexus.test', 'Fat Loss HIIT',      'HIIT',       'HIIT Circuit',      'Conditioning circuit session.',     '2026-06-14 07:00', '2026-06-14 07:45', 'WEEKLY', 'Branch 1 - Studio',     'COMPLETED', 'Completed all rounds.'),
    ('trainer2@fitnexus.test', 'user3@fitnexus.test', 'Beginner Full Body', 'Functional', 'Mobility & Core',   'Yoga-style mobility session.',      '2026-06-21 09:00', '2026-06-21 10:00', 'WEEKLY', 'Branch 4 - Studio A',   'PENDING',   'Focus on flexibility.')
) AS v(trainer_email, user_email, plan_name, wtype, title, description, start_dt, end_dt, repeat_type, location, completion, notes)
WHERE NOT EXISTS (
    SELECT 1 FROM user_workout_schedules u
    WHERE u.title = v.title
      AND u.user_id = (SELECT id FROM user_accounts WHERE email = v.user_email)
      AND u.start_date_time = v.start_dt::timestamp
);

COMMIT;

-- -----------------------------------------------------------------------------
-- Optionally assign plans to demo customers so /me/workout-plan and
-- /me/diet-plan return data immediately. Uncomment to apply:
--
-- UPDATE user_accounts SET assigned_workout_plan_id = (SELECT id FROM workout_plans WHERE name = 'Beginner Full Body')
--   WHERE email = 'user1@fitnexus.test';
-- UPDATE user_accounts SET assigned_diet_plan_id = (SELECT id FROM diet_plans WHERE name = 'High Protein Breakfast' LIMIT 1)
--   WHERE email = 'user1@fitnexus.test';
-- -----------------------------------------------------------------------------
