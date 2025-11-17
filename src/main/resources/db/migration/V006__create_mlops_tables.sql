-- Migration to create MLOps batch metrics tables
-- Version: V006 - Create mlops_batch, mlops_job, and mlops_job_metrics tables

-- Create mlops_batch table
CREATE TABLE IF NOT EXISTS mlops_batch (
    id BIGSERIAL PRIMARY KEY,

    -- DVC Configuration
    testing_data_nvrs_version VARCHAR(100) NOT NULL,
    prompts_version VARCHAR(100) NOT NULL,
    known_non_issues_version VARCHAR(100) NOT NULL,
    container_image VARCHAR(500) NOT NULL,

    -- Metadata
    submitted_by VARCHAR(255),
    submitted_at TIMESTAMP NOT NULL,
    last_updated_at TIMESTAMP,

    -- Status Tracking
    status VARCHAR(50) NOT NULL,
    total_jobs INTEGER,
    completed_jobs INTEGER,
    failed_jobs INTEGER
);

-- Create mlops_job table
CREATE TABLE IF NOT EXISTS mlops_job (
    id BIGSERIAL PRIMARY KEY,
    mlops_batch_id BIGINT NOT NULL REFERENCES mlops_batch(id),

    -- Package Information
    package_nvr VARCHAR(255) NOT NULL,
    package_name VARCHAR(255),
    project_name VARCHAR(255),
    project_version VARCHAR(255),

    -- URLs
    package_source_code_url VARCHAR(255),
    known_false_positives_url VARCHAR(255),
    tekton_url VARCHAR(255),

    -- Status & Timestamps
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    last_updated_at TIMESTAMP,

    -- Optional Fields
    submitted_by VARCHAR(255),
    hostname VARCHAR(255),
    osh_scan_id VARCHAR(255),
    jira_link VARCHAR(255)
);

-- Create mlops_job_metrics table
CREATE TABLE IF NOT EXISTS mlops_job_metrics (
    id BIGSERIAL PRIMARY KEY,
    mlops_job_id BIGINT NOT NULL REFERENCES mlops_job(id) ON DELETE CASCADE,

    package_name VARCHAR(255) NOT NULL,

    -- Aggregated Metrics
    accuracy NUMERIC(5, 4),      -- e.g., 0.9523
    precision NUMERIC(5, 4),     -- e.g., 0.8750
    recall NUMERIC(5, 4),        -- e.g., 0.9200
    f1_score NUMERIC(5, 4),      -- e.g., 0.8970

    -- Confusion Matrix
    cm_tp INTEGER,               -- True Positives
    cm_fp INTEGER,               -- False Positives
    cm_tn INTEGER,               -- True Negatives
    cm_fn INTEGER,               -- False Negatives

    created_at TIMESTAMP NOT NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_mlops_batch_id ON mlops_batch(id);
CREATE INDEX IF NOT EXISTS idx_mlops_batch_status ON mlops_batch(status);
CREATE INDEX IF NOT EXISTS idx_mlops_batch_submitted_at ON mlops_batch(submitted_at);

CREATE INDEX IF NOT EXISTS idx_mlops_job_id ON mlops_job(id);
CREATE INDEX IF NOT EXISTS idx_mlops_job_mlops_batch_id ON mlops_job(mlops_batch_id);
CREATE INDEX IF NOT EXISTS idx_mlops_job_status ON mlops_job(status);
CREATE INDEX IF NOT EXISTS idx_mlops_job_created_at ON mlops_job(created_at);
CREATE INDEX IF NOT EXISTS idx_mlops_job_osh_scan_id ON mlops_job(osh_scan_id);

CREATE INDEX IF NOT EXISTS idx_mlops_job_metrics_id ON mlops_job_metrics(id);
CREATE INDEX IF NOT EXISTS idx_mlops_job_metrics_mlops_job_id ON mlops_job_metrics(mlops_job_id);
CREATE INDEX IF NOT EXISTS idx_mlops_job_metrics_package_name ON mlops_job_metrics(package_name);

-- Comments for documentation
COMMENT ON TABLE mlops_batch IS 'MLOps batch execution tracking with DVC versioning';
COMMENT ON TABLE mlops_job IS 'Individual MLOps job execution tracking';
COMMENT ON TABLE mlops_job_metrics IS 'ML performance metrics for each job execution';