-- Cleanup migration to remove placeholder test record from V1_1_0
-- Version: V016 - Remove invalid placeholder record from osh_uncollected_scan

-- Remove the placeholder record that was inserted in V1_1_0
-- This record had invalid JSON structure that caused NullPointerException
-- in the retry processor due to missing scanId field
-- The combination of osh_scan_id = 0 and failure_reason uniquely identifies this placeholder
DELETE FROM osh_uncollected_scan
WHERE osh_scan_id = 0
  AND failure_reason = 'UNKNOWN_ERROR';

-- Verify cleanup
-- Expected result: 0 rows (the placeholder should be gone)
SELECT COUNT(*) as remaining_placeholder_records
FROM osh_uncollected_scan 
WHERE osh_scan_id = 0;

