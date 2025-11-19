-- Migration to update input_source_type constraint to include OSH_SCAN
-- Version: V1.3.0 - Add OSH_SCAN to input_source_type check constraint

-- Drop the old constraint
ALTER TABLE job DROP CONSTRAINT IF EXISTS job_input_source_type_check;

-- Add the updated constraint that includes OSH_SCAN
ALTER TABLE job ADD CONSTRAINT job_input_source_type_check
    CHECK (input_source_type IN ('SARIF', 'GOOGLE_SHEET', 'OSH_SCAN'));

-- Add comment for documentation
COMMENT ON CONSTRAINT job_input_source_type_check ON job IS
    'Validates that input_source_type matches one of the allowed InputSourceType enum values: SARIF, GOOGLE_SHEET, or OSH_SCAN';
