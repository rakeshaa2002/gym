-- Normalize uploaded document columns from *_url to *_path and remove obsolete detailed education fields.
DO $$
DECLARE
    tbl text;
    pair text[];
BEGIN
    FOREACH tbl IN ARRAY ARRAY['admins', 'managers', 'trainers'] LOOP
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS qualification_document_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS certification_document_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS id_proof_document_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS address_proof_document_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS resume_document_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS offer_letter_document_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS candidate_photo_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS aadhar_card_document_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS pan_card_document_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS bank_document_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS experience_certificate_document_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS course_certificate_path VARCHAR(500)', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS education_certificate_path VARCHAR(500)', tbl);

        FOREACH pair SLICE 1 IN ARRAY ARRAY[
            ARRAY['qualification_document_path', 'qualification_document_url'],
            ARRAY['certification_document_path', 'certification_document_url'],
            ARRAY['id_proof_document_path', 'id_proof_document_url'],
            ARRAY['address_proof_document_path', 'address_proof_document_url'],
            ARRAY['resume_document_path', 'resume_document_url'],
            ARRAY['offer_letter_document_path', 'offer_letter_document_url'],
            ARRAY['candidate_photo_path', 'candidate_photo_url'],
            ARRAY['aadhar_card_document_path', 'aadhar_card_document_url'],
            ARRAY['pan_card_document_path', 'pan_card_document_url'],
            ARRAY['bank_document_path', 'bank_document_url'],
            ARRAY['experience_certificate_document_path', 'experience_certificate_document_url'],
            ARRAY['education_certificate_path', 'education_certificate_url']
        ] LOOP
            IF EXISTS (
                SELECT 1
                FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name = tbl
                  AND column_name = pair[2]
            ) THEN
                EXECUTE format('UPDATE %I SET %I = COALESCE(%I, %I)', tbl, pair[1], pair[1], pair[2]);
            END IF;
        END LOOP;

        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS qualification_document_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS certification_document_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS id_proof_document_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS address_proof_document_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS resume_document_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS offer_letter_document_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS candidate_photo_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS aadhar_card_document_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS pan_card_document_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS bank_document_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS experience_certificate_document_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS course', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS education_certificate_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS graduation_type', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS graduation_details', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS graduation_certificate_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS graduation_marksheet_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS hsc_details', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS hsc_marksheet_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS sslc_details', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS sslc_marksheet_url', tbl);
        EXECUTE format('ALTER TABLE IF EXISTS %I DROP COLUMN IF EXISTS community_certificate_url', tbl);
    END LOOP;
END $$;

