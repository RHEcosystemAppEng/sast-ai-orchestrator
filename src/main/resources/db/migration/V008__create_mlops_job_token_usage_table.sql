-- Migration to create mlops_job_token_usage table
-- Version: V008 - Add mlops_job_token_usage table for storing token usage and timing metrics

-- Create mlops_job_token_usage table
CREATE TABLE IF NOT EXISTS mlops_job_token_usage (
    id BIGINT PRIMARY KEY,
    total_input_tokens INTEGER NOT NULL DEFAULT 0,
    total_output_tokens INTEGER NOT NULL DEFAULT 0,
    total_tokens INTEGER NOT NULL DEFAULT 0,
    total_duration_seconds DECIMAL(10, 3),
    node_breakdown JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mlops_job_token_usage_mlops_job
        FOREIGN KEY (id) REFERENCES mlops_job(id) ON DELETE CASCADE
);

-- Create index as defined in the entity pattern
CREATE INDEX IF NOT EXISTS idx_mlops_job_token_usage_id ON mlops_job_token_usage(id);

-- Create GIN index on JSONB column for efficient querying of node breakdown
CREATE INDEX IF NOT EXISTS idx_mlops_job_token_usage_node_breakdown
    ON mlops_job_token_usage USING GIN (node_breakdown);

-- Comments for documentation
COMMENT ON TABLE mlops_job_token_usage IS 'Stores token usage and timing metrics for MLOps jobs (one row per pipeline run)';
COMMENT ON COLUMN mlops_job_token_usage.id IS 'Shared primary key with mlops_job (one-to-one relationship)';
COMMENT ON COLUMN mlops_job_token_usage.total_input_tokens IS 'Total input/prompt tokens across all LLM calls in the pipeline run';
COMMENT ON COLUMN mlops_job_token_usage.total_output_tokens IS 'Total output/completion tokens across all LLM calls in the pipeline run';
COMMENT ON COLUMN mlops_job_token_usage.total_tokens IS 'Total tokens (input + output) for the entire pipeline run';
COMMENT ON COLUMN mlops_job_token_usage.total_duration_seconds IS 'Total execution duration in seconds (sum of all node durations)';
COMMENT ON COLUMN mlops_job_token_usage.node_breakdown IS 'JSONB array with per-node metrics from Tekton result: [{tool_name, model, input_tokens, output_tokens, total_tokens, duration_seconds}]';
COMMENT ON COLUMN mlops_job_token_usage.created_at IS 'Timestamp when the record was created';