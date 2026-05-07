-- Optional onboarding fields from the employee joining form plus branch-aware reporting relationship.
-- These fields are intentionally nullable so employee data can be completed later.

ALTER TABLE IF EXISTS user_accounts
    ADD COLUMN IF NOT EXISTS reports_to_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_accounts_reports_to'
    ) THEN
        ALTER TABLE user_accounts
            ADD CONSTRAINT fk_user_accounts_reports_to
            FOREIGN KEY (reports_to_id) REFERENCES user_accounts(id);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_user_accounts_reports_to_id ON user_accounts(reports_to_id);
CREATE INDEX IF NOT EXISTS idx_user_accounts_branch_role ON user_accounts(branch_id, role);

ALTER TABLE IF EXISTS admins
    ADD COLUMN IF NOT EXISTS father_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS mother_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS marital_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS spouse_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS location VARCHAR(100),
    ADD COLUMN IF NOT EXISTS candidate_photo_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS aadhar_card_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS pan_card_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS bank_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS bank_account_holder_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bank_branch VARCHAR(150),
    ADD COLUMN IF NOT EXISTS previous_employment_1 VARCHAR(500),
    ADD COLUMN IF NOT EXISTS previous_employment_2 VARCHAR(500),
    ADD COLUMN IF NOT EXISTS experience_certificate_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS graduation_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS graduation_details VARCHAR(300),
    ADD COLUMN IF NOT EXISTS graduation_certificate_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS graduation_marksheet_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS hsc_details VARCHAR(300),
    ADD COLUMN IF NOT EXISTS hsc_marksheet_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS sslc_details VARCHAR(300),
    ADD COLUMN IF NOT EXISTS sslc_marksheet_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS community_certificate_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS emergency_contact_relationship VARCHAR(80),
    ADD COLUMN IF NOT EXISTS emergency_contact_name_2 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS emergency_contact_relationship_2 VARCHAR(80),
    ADD COLUMN IF NOT EXISTS emergency_phone_2 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS reference_name_1 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reference_phone_1 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS reference_name_2 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reference_phone_2 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS joining_branch_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS source_platform VARCHAR(80),
    ADD COLUMN IF NOT EXISTS pf_uan VARCHAR(30),
    ADD COLUMN IF NOT EXISTS esi_number VARCHAR(30),
    ADD COLUMN IF NOT EXISTS declaration_date DATE,
    ADD COLUMN IF NOT EXISTS declaration_place VARCHAR(100);

ALTER TABLE IF EXISTS managers
    ADD COLUMN IF NOT EXISTS father_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS mother_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS marital_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS spouse_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS location VARCHAR(100),
    ADD COLUMN IF NOT EXISTS candidate_photo_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS aadhar_card_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS pan_card_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS bank_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS bank_account_holder_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bank_branch VARCHAR(150),
    ADD COLUMN IF NOT EXISTS previous_employment_1 VARCHAR(500),
    ADD COLUMN IF NOT EXISTS previous_employment_2 VARCHAR(500),
    ADD COLUMN IF NOT EXISTS experience_certificate_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS graduation_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS graduation_details VARCHAR(300),
    ADD COLUMN IF NOT EXISTS graduation_certificate_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS graduation_marksheet_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS hsc_details VARCHAR(300),
    ADD COLUMN IF NOT EXISTS hsc_marksheet_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS sslc_details VARCHAR(300),
    ADD COLUMN IF NOT EXISTS sslc_marksheet_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS community_certificate_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS emergency_contact_relationship VARCHAR(80),
    ADD COLUMN IF NOT EXISTS emergency_contact_name_2 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS emergency_contact_relationship_2 VARCHAR(80),
    ADD COLUMN IF NOT EXISTS emergency_phone_2 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS reference_name_1 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reference_phone_1 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS reference_name_2 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reference_phone_2 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS joining_branch_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS source_platform VARCHAR(80),
    ADD COLUMN IF NOT EXISTS pf_uan VARCHAR(30),
    ADD COLUMN IF NOT EXISTS esi_number VARCHAR(30),
    ADD COLUMN IF NOT EXISTS declaration_date DATE,
    ADD COLUMN IF NOT EXISTS declaration_place VARCHAR(100);

ALTER TABLE IF EXISTS trainers
    ADD COLUMN IF NOT EXISTS father_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS mother_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS marital_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS spouse_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS location VARCHAR(100),
    ADD COLUMN IF NOT EXISTS candidate_photo_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS aadhar_card_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS pan_card_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS bank_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS bank_account_holder_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bank_branch VARCHAR(150),
    ADD COLUMN IF NOT EXISTS previous_employment_1 VARCHAR(500),
    ADD COLUMN IF NOT EXISTS previous_employment_2 VARCHAR(500),
    ADD COLUMN IF NOT EXISTS experience_certificate_document_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS graduation_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS graduation_details VARCHAR(300),
    ADD COLUMN IF NOT EXISTS graduation_certificate_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS graduation_marksheet_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS hsc_details VARCHAR(300),
    ADD COLUMN IF NOT EXISTS hsc_marksheet_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS sslc_details VARCHAR(300),
    ADD COLUMN IF NOT EXISTS sslc_marksheet_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS community_certificate_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS emergency_contact_relationship VARCHAR(80),
    ADD COLUMN IF NOT EXISTS emergency_contact_name_2 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS emergency_contact_relationship_2 VARCHAR(80),
    ADD COLUMN IF NOT EXISTS emergency_phone_2 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS reference_name_1 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reference_phone_1 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS reference_name_2 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reference_phone_2 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS joining_branch_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS source_platform VARCHAR(80),
    ADD COLUMN IF NOT EXISTS pf_uan VARCHAR(30),
    ADD COLUMN IF NOT EXISTS esi_number VARCHAR(30),
    ADD COLUMN IF NOT EXISTS declaration_date DATE,
    ADD COLUMN IF NOT EXISTS declaration_place VARCHAR(100);
