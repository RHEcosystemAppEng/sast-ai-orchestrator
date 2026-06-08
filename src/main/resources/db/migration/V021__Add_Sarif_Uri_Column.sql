-- Add sarif_uri column for debugging SARIF report sources in Konflux scans
ALTER TABLE job ADD COLUMN IF NOT EXISTS sarif_uri VARCHAR(512);

-- Add comment for documentation
COMMENT ON COLUMN job.sarif_uri IS
    'Full OCI artifact URI for Konflux scans (e.g., registry.com/namespace/repo@sha256:hash)';
