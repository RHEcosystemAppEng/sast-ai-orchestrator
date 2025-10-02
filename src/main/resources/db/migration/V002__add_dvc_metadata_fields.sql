-- Migration to add DVC metadata tracking fields and data artifacts table
-- Version: V002 - Add DVC metadata fields and artifact tracking

-- Create data_artifacts table following DVC schema specifications
CREATE TABLE IF NOT EXISTS data_artifacts (
    artifact_id VARCHAR(255) PRIMARY KEY,
    artifact_type VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(100) NOT NULL,
    dvc_path VARCHAR(500) NOT NULL,
    dvc_hash VARCHAR(255) NOT NULL,
    metadata JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- DVC basic metadata to job table
ALTER TABLE job ADD COLUMN IF NOT EXISTS dvc_data_version VARCHAR(255);
ALTER TABLE job ADD COLUMN IF NOT EXISTS dvc_pipeline_stage VARCHAR(100);
ALTER TABLE job ADD COLUMN IF NOT EXISTS dvc_commit_hash VARCHAR(40);

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_data_artifacts_type ON data_artifacts(artifact_type);
CREATE INDEX IF NOT EXISTS idx_data_artifacts_created_at ON data_artifacts(created_at);
CREATE INDEX IF NOT EXISTS idx_job_dvc_data_version ON job(dvc_data_version);
CREATE INDEX IF NOT EXISTS idx_job_dvc_commit_hash ON job(dvc_commit_hash);

-- Comments for documentation
COMMENT ON TABLE data_artifacts IS 'DVC-tracked data artifacts with lineage and versioning information';
COMMENT ON COLUMN data_artifacts.artifact_type IS 'Type: package, filter_rules, embedding_model, configuration, output';
COMMENT ON COLUMN data_artifacts.dvc_path IS 'DVC file path for artifact tracking';
COMMENT ON COLUMN data_artifacts.dvc_hash IS 'DVC hash for content-based versioning';
COMMENT ON COLUMN data_artifacts.metadata IS 'JSON metadata including split_type, source_code_repo, etc.';

COMMENT ON COLUMN job.dvc_data_version IS 'DVC data version tag for reproducibility tracking';
COMMENT ON COLUMN job.dvc_pipeline_stage IS 'DVC pipeline stage name that generated this job';
COMMENT ON COLUMN job.dvc_commit_hash IS 'Git commit hash when DVC pipeline was executed';