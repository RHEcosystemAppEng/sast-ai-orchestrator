-- Migration to add KONFLUX_SCAN to input_source_type constraint
-- Version: V020 - Add KONFLUX_SCAN support for Konflux Trusted Artifacts integration

-- Drop the old constraint
ALTER TABLE job DROP CONSTRAINT IF EXISTS job_input_source_type_check;

-- Add the updated constraint that includes KONFLUX_SCAN
ALTER TABLE job ADD CONSTRAINT job_input_source_type_check
    CHECK (input_source_type IN ('SARIF', 'GOOGLE_SHEET', 'OSH_SCAN', 'KONFLUX_SCAN'));

-- Add comment for documentation
COMMENT ON CONSTRAINT job_input_source_type_check ON job IS
    'Validates that input_source_type matches one of the allowed InputSourceType enum values: SARIF, GOOGLE_SHEET, OSH_SCAN, or KONFLUX_SCAN';
