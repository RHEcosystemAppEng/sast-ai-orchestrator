-- Add sarif_digest column for debugging SARIF report sources in Konflux scans
ALTER TABLE job ADD COLUMN IF NOT EXISTS sarif_digest VARCHAR(512);

-- Add comment for documentation
COMMENT ON COLUMN job.sarif_digest IS
    'SARIF report digest for Konflux scans (sha256 hash of SARIF file downloaded from Trusted Artifacts)';
