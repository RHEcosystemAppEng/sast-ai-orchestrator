-- Add MLOps tracking and metadata tables
-- Version: V008 - Add workflow graph topology, job batch run definitions, execution context, data flow, metrics, token usage, pricing models, and scheduler cursor

-- Create workflow_graph_topology table
CREATE TABLE IF NOT EXISTS workflow_graph_topology (
    id BIGSERIAL PRIMARY KEY,
    topology_id INTEGER UNIQUE NOT NULL,
    description VARCHAR(255),
    edges JSONB NOT NULL
);

-- Create job_batch_run_definitions table
CREATE TABLE IF NOT EXISTS job_batch_run_definitions (
    id BIGSERIAL PRIMARY KEY,
    definition_id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    git_hash VARCHAR(40) NOT NULL,
    description TEXT,
    config_version VARCHAR(50) NOT NULL,
    workflow_graph_topology_id BIGINT REFERENCES workflow_graph_topology(id),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_job_batch_run_def_id ON job_batch_run_definitions(id);

-- Add job_batch_run_definition_id column to job_batch table (if not exists)
ALTER TABLE job_batch
ADD COLUMN IF NOT EXISTS job_batch_run_definition_id BIGINT;

-- Add foreign key constraint to job_batch_run_definitions table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_job_batch_run_definition'
    ) THEN
        ALTER TABLE job_batch
        ADD CONSTRAINT fk_job_batch_run_definition
        FOREIGN KEY (job_batch_run_definition_id)
        REFERENCES job_batch_run_definitions(id);
    END IF;
END $$;

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_job_batch_run_definition_id ON job_batch(job_batch_run_definition_id);

-- Create job_batch_execution_context table (1:1 with job_batch)
CREATE TABLE IF NOT EXISTS job_batch_execution_context (
    id BIGINT PRIMARY KEY REFERENCES job_batch(id),
    environment VARCHAR(100) NOT NULL,
    config_version VARCHAR(50) NOT NULL,
    hw_spec JSONB NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_job_batch_execution_context_id ON job_batch_execution_context(id);

-- Create job_batch_data_flow table (1:1 with job_batch)
CREATE TABLE IF NOT EXISTS job_batch_data_flow (
    id BIGINT PRIMARY KEY REFERENCES job_batch(id),
    flow_id VARCHAR(255) UNIQUE NOT NULL,
    input_artifacts JSONB NOT NULL,
    transformation_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_job_batch_data_flow_id ON job_batch_data_flow(id);

-- Create job_token_usage table (1:1 with job)
CREATE TABLE IF NOT EXISTS job_token_usage (
    id BIGINT PRIMARY KEY REFERENCES job(id),
    total_input_tokens INTEGER NOT NULL,
    total_output_tokens INTEGER NOT NULL,
    total_tokens INTEGER NOT NULL,
    node_breakdown JSONB NOT NULL,
    estimated_cost NUMERIC(10,4) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_job_token_usage_id ON job_token_usage(id);

-- Create job_metrics table (1:1 with job)
CREATE TABLE IF NOT EXISTS job_metrics (
    id BIGINT PRIMARY KEY REFERENCES job(id),
    package_name VARCHAR(255) NOT NULL,
    total_issues INTEGER NOT NULL,
    predicted_issues_count INTEGER NOT NULL,
    predicted_non_issues_count INTEGER NOT NULL,
    actual_issues_count INTEGER,
    actual_non_issues_count INTEGER,
    has_ground_truth BOOLEAN NOT NULL DEFAULT false,
    precision NUMERIC(5,4),
    recall NUMERIC(5,4),
    f1_score NUMERIC(5,4),
    accuracy NUMERIC(5,4),
    confusion_matrix JSONB,
    node_metrics JSONB,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_job_metrics_id ON job_metrics(id);

-- Create osh_scheduler_cursor table
CREATE TABLE IF NOT EXISTS osh_scheduler_cursor (
    id BIGSERIAL PRIMARY KEY,
    last_seen_token VARCHAR(255),
    last_seen_timestamp TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
);

-- Create pricing_models table
CREATE TABLE IF NOT EXISTS pricing_models (
    pricing_model_id VARCHAR(100) PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    input_price_per_1k NUMERIC(8,6) NOT NULL,
    output_price_per_1k NUMERIC(8,6) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_pricing_models_id ON pricing_models(pricing_model_id);

