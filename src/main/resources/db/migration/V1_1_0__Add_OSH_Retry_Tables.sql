-- Migration script for OSH Retry mechanism - V1.1.0
-- Creates the osh_uncollected_scan table and associated indexes

-- Create the OSH uncollected scan table for retry queue management
CREATE TABLE IF NOT EXISTS osh_uncollected_scan (
    id BIGSERIAL PRIMARY KEY,
    osh_scan_id INTEGER NOT NULL,
    package_name VARCHAR(255),
    package_nvr VARCHAR(512),
    failure_reason VARCHAR(50) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    last_attempt_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    scan_data_json TEXT,
    last_error_message TEXT
);

-- Add unique constraint on osh_scan_id to prevent duplicate retry entries
ALTER TABLE osh_uncollected_scan
ADD CONSTRAINT uk_osh_uncollected_scan_id UNIQUE (osh_scan_id);

-- Create indexes for optimal query performance

-- Index for unique constraint and scan ID lookups
CREATE INDEX IF NOT EXISTS idx_osh_uncollected_scan_id
ON osh_uncollected_scan(osh_scan_id);

-- Composite index optimized for retry eligibility query:
-- WHERE last_attempt_at < :cutoff AND attempt_count < :max
-- This index supports the main retry selection query efficiently
CREATE INDEX IF NOT EXISTS idx_osh_uncollected_retry_eligible
ON osh_uncollected_scan(last_attempt_at, attempt_count, failure_reason);

-- Index for retention cleanup queries:
-- WHERE created_at < :cutoff
CREATE INDEX IF NOT EXISTS idx_osh_uncollected_cleanup
ON osh_uncollected_scan(created_at);

-- Additional performance indexes for common queries

-- Index for package-specific queries (debugging and monitoring)
CREATE INDEX IF NOT EXISTS idx_osh_uncollected_package
ON osh_uncollected_scan(package_name);

-- Index for failure reason analysis
CREATE INDEX IF NOT EXISTS idx_osh_uncollected_failure_reason
ON osh_uncollected_scan(failure_reason);

-- Index for attempt count monitoring
CREATE INDEX IF NOT EXISTS idx_osh_uncollected_attempt_count
ON osh_uncollected_scan(attempt_count);

-- Comments for table and columns
COMMENT ON TABLE osh_uncollected_scan IS
'Retry queue for OSH scans that failed processing. Stores failed scans for retry attempts with backoff and attempt limits.';

COMMENT ON COLUMN osh_uncollected_scan.osh_scan_id IS
'OSH scan ID that failed processing (unique to prevent duplicate retries)';

COMMENT ON COLUMN osh_uncollected_scan.package_name IS
'Package name from OSH scan metadata (for monitoring and debugging)';

COMMENT ON COLUMN osh_uncollected_scan.package_nvr IS
'Package NVR if successfully parsed before failure occurred';

COMMENT ON COLUMN osh_uncollected_scan.failure_reason IS
'Classification of failure type (for monitoring and debugging)';

COMMENT ON COLUMN osh_uncollected_scan.attempt_count IS
'Number of retry attempts made (used to enforce max retry limits)';

COMMENT ON COLUMN osh_uncollected_scan.created_at IS
'When this scan was first recorded as failed (for retention policies)';

COMMENT ON COLUMN osh_uncollected_scan.last_attempt_at IS
'When the most recent retry attempt was made (for backoff calculations)';

COMMENT ON COLUMN osh_uncollected_scan.version IS
'Optimistic locking version field (prevents concurrent modification conflicts)';

COMMENT ON COLUMN osh_uncollected_scan.scan_data_json IS
'Original OSH scan response as JSON (avoids re-fetching from OSH during retry)';

COMMENT ON COLUMN osh_uncollected_scan.last_error_message IS
'Error message from most recent failure (for debugging and analysis)';

-- Insert initial statistics (optional - for monitoring setup)
-- This can be removed if not needed
INSERT INTO osh_uncollected_scan (osh_scan_id, failure_reason, attempt_count, created_at, last_attempt_at, scan_data_json)
VALUES (0, 'UNKNOWN_ERROR', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '{"note": "Initial placeholder record for testing"}')
ON CONFLICT (osh_scan_id) DO NOTHING;