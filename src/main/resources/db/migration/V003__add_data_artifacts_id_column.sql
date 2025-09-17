-- Migration to add missing id column to data_artifacts table
-- Version: V003 - Add auto-incrementing primary key id column

-- Add the id column as auto-incrementing primary key
ALTER TABLE data_artifacts ADD COLUMN id BIGSERIAL;

-- Drop the existing primary key on artifact_id
ALTER TABLE data_artifacts DROP CONSTRAINT data_artifacts_pkey;

-- Set the new id column as primary key
ALTER TABLE data_artifacts ADD PRIMARY KEY (id);

-- Add unique constraint on artifact_id to maintain uniqueness
ALTER TABLE data_artifacts ADD CONSTRAINT uk_data_artifacts_artifact_id UNIQUE (artifact_id);

-- Add index for the artifact_id for performance
CREATE INDEX IF NOT EXISTS idx_data_artifacts_artifact_id ON data_artifacts(artifact_id);

-- Comments for documentation
COMMENT ON COLUMN data_artifacts.id IS 'Auto-generated primary key for JPA entity mapping';
COMMENT ON CONSTRAINT uk_data_artifacts_artifact_id ON data_artifacts IS 'Unique constraint on business identifier artifact_id';