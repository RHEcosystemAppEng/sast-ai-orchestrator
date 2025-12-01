-- Migration: Create mlops_job_node_filter_eval table
-- Description: Stores filter node evaluation results from Tekton pipeline runs
-- Filter node evaluates FAISS vector search performance for finding similar known issues

CREATE TABLE IF NOT EXISTS mlops_job_node_filter_eval (
    id BIGINT PRIMARY KEY,
    faiss_stratified_stats JSONB NOT NULL DEFAULT '{}'::jsonb,
    total_tokens INTEGER DEFAULT 0,
    llm_call_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mlops_job_node_filter_eval_mlops_job
        FOREIGN KEY (id) REFERENCES mlops_job(id) ON DELETE CASCADE
);

-- Index for JSONB queries on FAISS stats
CREATE INDEX IF NOT EXISTS idx_mlops_job_node_filter_eval_faiss_stats
    ON mlops_job_node_filter_eval USING GIN (faiss_stratified_stats);

COMMENT ON TABLE mlops_job_node_filter_eval IS 'Filter node evaluation results - FAISS vector search performance';
COMMENT ON COLUMN mlops_job_node_filter_eval.id IS 'Shared primary key with mlops_job (one-to-one relationship)';
COMMENT ON COLUMN mlops_job_node_filter_eval.faiss_stratified_stats IS 'FAISS matching statistics stratified by categories';
COMMENT ON COLUMN mlops_job_node_filter_eval.total_tokens IS 'Total tokens used by filter node';
COMMENT ON COLUMN mlops_job_node_filter_eval.llm_call_count IS 'Number of LLM calls made by filter node';