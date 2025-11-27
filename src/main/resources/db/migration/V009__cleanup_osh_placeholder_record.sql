-- Cleanup migration to remove placeholder test record from V1_1_0
-- Version: V009 - Remove invalid placeholder record from osh_uncollected_scan

-- Remove the placeholder record that was inserted in V1_1_0
-- This record had invalid JSON structure that caused NullPointerException
-- in the retry processor due to missing scanId field
DELETE FROM osh_uncollected_scan 
WHERE osh_scan_id = 0 
  AND failure_reason = 'UNKNOWN_ERROR'
  AND scan_data_json LIKE '%Initial placeholder record%';

-- Verify cleanup
-- Expected result: 0 rows (the placeholder should be gone)
SELECT COUNT(*) as remaining_placeholder_records
FROM osh_uncollected_scan 
WHERE osh_scan_id = 0;

