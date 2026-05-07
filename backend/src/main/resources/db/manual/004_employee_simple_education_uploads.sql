ALTER TABLE IF EXISTS admins
    ADD COLUMN IF NOT EXISTS course_certificate_path VARCHAR(500),
    ADD COLUMN IF NOT EXISTS education_certificate_path VARCHAR(500);

ALTER TABLE IF EXISTS managers
    ADD COLUMN IF NOT EXISTS course_certificate_path VARCHAR(500),
    ADD COLUMN IF NOT EXISTS education_certificate_path VARCHAR(500);

ALTER TABLE IF EXISTS trainers
    ADD COLUMN IF NOT EXISTS course_certificate_path VARCHAR(500),
    ADD COLUMN IF NOT EXISTS education_certificate_path VARCHAR(500);

