-- Migration to add MLOps batch infrastructure for independent MLOps workflow tracking
-- Version: V004 - Add MLOps batch, job, and metrics tables

-- Create mlops_batch table
CREATE TABLE IF NOT EXISTS mlops_batch (
    id BIGSERIAL PRIMARY KEY,
    testing_data_nvrs_version VARCHAR(100) NOT NULL,
    prompts_version VARCHAR(100) NOT NULL,
    known_non_issues_version VARCHAR(100) NOT NULL,
    container_image VARCHAR(500) NOT NULL,
    submitted_by VARCHAR(255),
    submitted_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_jobs INTEGER,
    completed_jobs INTEGER,
    failed_jobs INTEGER,
    last_updated_at TIMESTAMP
);

-- Create mlops_job table
CREATE TABLE IF NOT EXISTS mlops_job (
    id BIGSERIAL PRIMARY KEY,
    mlops_batch_id BIGINT NOT NULL REFERENCES mlops_batch(id),
    package_nvr VARCHAR(255) NOT NULL,
    project_name VARCHAR(255),
    project_version VARCHAR(255),
    package_name VARCHAR(255),
    package_source_code_url VARCHAR(255),
    known_false_positives_url VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    tekton_url VARCHAR(255),
    last_updated_at TIMESTAMP,
    submitted_by VARCHAR(255),
    hostname VARCHAR(255),
    osh_scan_id VARCHAR(255),
    jira_link VARCHAR(255)
);

-- Create mlops_job_metrics table
CREATE TABLE IF NOT EXISTS mlops_job_metrics (
    id BIGSERIAL PRIMARY KEY,
    mlops_job_id BIGINT NOT NULL REFERENCES mlops_job(id),
    package_name VARCHAR(255) NOT NULL,
    accuracy NUMERIC(5, 4),
    precision NUMERIC(5, 4),
    recall NUMERIC(5, 4),
    f1_score NUMERIC(5, 4),
    cm_tp INTEGER,
    cm_fp INTEGER,
    cm_tn INTEGER,
    cm_fn INTEGER,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_mlops_job_metrics FOREIGN KEY (mlops_job_id) REFERENCES mlops_job(id) ON DELETE CASCADE
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
COMMENT ON TABLE mlops_batch IS 'MLOps batch runs for DVC-managed NVR testing';
COMMENT ON COLUMN mlops_batch.testing_data_nvrs_version IS 'DVC version for testing data NVRs';
COMMENT ON COLUMN mlops_batch.prompts_version IS 'DVC version for prompts configuration';
COMMENT ON COLUMN mlops_batch.known_non_issues_version IS 'DVC version for known non-issues';
COMMENT ON COLUMN mlops_batch.container_image IS 'Container image used for SAST AI workflow';

COMMENT ON TABLE mlops_job IS 'Individual MLOps jobs for each NVR in a batch';
COMMENT ON COLUMN mlops_job.package_nvr IS 'Package Name-Version-Release string';

COMMENT ON TABLE mlops_job_metrics IS 'Stores aggregated metrics from MLOps job pipeline results';
COMMENT ON COLUMN mlops_job_metrics.accuracy IS 'Accuracy metric from workflow-metrics result';
COMMENT ON COLUMN mlops_job_metrics.precision IS 'Precision metric from workflow-metrics result';
COMMENT ON COLUMN mlops_job_metrics.recall IS 'Recall metric from workflow-metrics result';
COMMENT ON COLUMN mlops_job_metrics.f1_score IS 'F1 score metric from workflow-metrics result';
COMMENT ON COLUMN mlops_job_metrics.cm_tp IS 'Confusion matrix: True Positives';
COMMENT ON COLUMN mlops_job_metrics.cm_fp IS 'Confusion matrix: False Positives';
COMMENT ON COLUMN mlops_job_metrics.cm_tn IS 'Confusion matrix: True Negatives';
COMMENT ON COLUMN mlops_job_metrics.cm_fn IS 'Confusion matrix: False Negatives';

