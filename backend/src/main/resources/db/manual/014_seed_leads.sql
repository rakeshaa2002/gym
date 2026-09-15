BEGIN;

INSERT INTO gym_leads (name, email, phone, status, source, fitness_goal, lead_score, conversion_probability, created_at, updated_at)
VALUES
    ('Rahul Sharma', 'rahul@example.com', '9876543210', 'NEW', 'WALK_IN', 'Weight Loss', 85, 70, NOW(), NOW()),
    ('Priya Patel', 'priya@example.com', '9876543211', 'CONTACTED', 'FACEBOOK', 'Muscle Gain', 65, 45, NOW(), NOW()),
    ('Amit Kumar', 'amit@example.com', '9876543212', 'INTERESTED', 'INSTAGRAM', 'General Fitness', 75, 60, NOW(), NOW()),
    ('Sneha Reddy', 'sneha@example.com', '9876543213', 'TRIAL_BOOKED', 'REFERRAL', 'Weight Loss', 92, 85, NOW(), NOW()),
    ('Vikram Singh', 'vikram@example.com', '9876543214', 'TRIAL_COMPLETED', 'WEBSITE', 'Body Building', 88, 75, NOW(), NOW()),
    ('Neha Gupta', 'neha@example.com', '9876543215', 'NEGOTIATION', 'CORPORATE', 'General Fitness', 95, 90, NOW(), NOW()),
    ('Karan Malhotra', 'karan@example.com', '9876543216', 'WON', 'WALK_IN', 'Muscle Gain', 98, 100, NOW(), NOW()),
    ('Riya Desai', 'riya@example.com', '9876543217', 'LOST', 'FACEBOOK', 'Weight Loss', 30, 10, NOW(), NOW()),
    ('Arjun Nair', 'arjun@example.com', '9876543218', 'NEW', 'WHATSAPP', 'General Fitness', 78, 55, NOW(), NOW()),
    ('Ananya Iyer', 'ananya@example.com', '9876543219', 'CONTACTED', 'GOOGLE_ADS', 'Weight Loss', 60, 40, NOW(), NOW()),
    ('Rohan Joshi', 'rohan@example.com', '9876543220', 'TRIAL_BOOKED', 'WALK_IN', 'Muscle Gain', 85, 80, NOW(), NOW()),
    ('Meera Kapoor', 'meera@example.com', '9876543221', 'WON', 'REFERRAL', 'General Fitness', 99, 100, NOW(), NOW());

COMMIT;
