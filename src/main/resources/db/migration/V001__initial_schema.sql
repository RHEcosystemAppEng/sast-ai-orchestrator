-- Initial schema creation for SAST-AI Orchestrator
-- Version: V001 - Create base tables

-- Create job_batch table
CREATE TABLE IF NOT EXISTS job_batch (
    id BIGSERIAL PRIMARY KEY,
    batch_google_sheet_url VARCHAR(255) NOT NULL,
    submitted_by VARCHAR(255),
    submitted_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_jobs INTEGER,
    completed_jobs INTEGER,
    failed_jobs INTEGER,
    last_updated_at TIMESTAMP,
    use_known_false_positive_file BOOLEAN
);

-- Create job table
CREATE TABLE IF NOT EXISTS job (
    id BIGSERIAL PRIMARY KEY,
    project_name VARCHAR(255),
    project_version VARCHAR(255),
    package_name VARCHAR(255),
    package_nvr VARCHAR(255),
    osh_scan_id VARCHAR(255),
    package_source_code_url VARCHAR(255),
    jira_link VARCHAR(255),
    hostname VARCHAR(255),
    known_false_positives_url VARCHAR(255),
    input_source_type VARCHAR(50),
    google_sheet_url VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    tekton_url VARCHAR(255),
    last_updated_at TIMESTAMP,
    submitted_by VARCHAR(255),
    job_batch_id BIGINT REFERENCES job_batch(id)
);

-- Create job_settings table
CREATE TABLE IF NOT EXISTS job_settings (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL REFERENCES job(id),
    llm_model_name VARCHAR(255),
    embeddings_model_name VARCHAR(255),
    calculate_ragas_metrics BOOLEAN,
    use_known_false_positive_file BOOLEAN,
    chunk_size INTEGER,
    chunk_overlap INTEGER,
    max_analysis_iterations INTEGER,
    settings_json TEXT
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_job_status ON job(status);
CREATE INDEX IF NOT EXISTS idx_job_created_at ON job(created_at);
CREATE INDEX IF NOT EXISTS idx_job_batch_status ON job_batch(status);
CREATE INDEX IF NOT EXISTS idx_job_batch_submitted_at ON job_batch(submitted_at);
CREATE INDEX IF NOT EXISTS idx_job_batch_id ON job(job_batch_id);