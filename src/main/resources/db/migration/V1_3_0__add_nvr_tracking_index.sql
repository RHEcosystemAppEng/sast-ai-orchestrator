-- Migration: V1_3_0__add_nvr_tracking_index.sql
-- Purpose: Add indexes for efficient NVR-based duplicate scan detection
-- Related: APPENG-4112 - Keep track of packages SAST-AI reviews
--
-- Duplicate detection logic:
--   - Match by: package_nvr + input_source_type + status
--   - Same NVR + same source type (OSH_SCAN or GOOGLE_SHEET) = duplicate
--   - Same NVR + different source type = NOT duplicate (allowed)

-- Composite index for NVR + input source type duplicate detection
-- Covers queries: findCompletedByNvrAndInputSourceType, findByNvrInputSourceTypeAndStatus
CREATE INDEX IF NOT EXISTS idx_job_nvr_source_type_status ON job(package_nvr, input_source_type, status);

-- Simple index on package_nvr for general NVR lookups
CREATE INDEX IF NOT EXISTS idx_job_package_nvr ON job(package_nvr);

-- Add comments for documentation
COMMENT ON INDEX idx_job_nvr_source_type_status IS 'Composite index for NVR + input source type duplicate detection - APPENG-4112';
COMMENT ON INDEX idx_job_package_nvr IS 'Index for general NVR lookups - APPENG-4112';
