-- Migration: Create mlops_job_node_summary_eval table
-- Description: Stores summary node evaluation results from Tekton pipeline runs
-- Summary node evaluates quality of LLM-generated summaries

CREATE TABLE IF NOT EXISTS mlops_job_node_summary_eval (
    id BIGINT PRIMARY KEY,
    overall_score DECIMAL(5, 4),
    semantic_similarity DECIMAL(5, 4),
    factual_accuracy DECIMAL(5, 4),
    conciseness DECIMAL(5, 4),
    professional_tone DECIMAL(5, 4),
    total_tokens INTEGER DEFAULT 0,
    llm_call_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mlops_job_node_summary_eval_mlops_job
        FOREIGN KEY (id) REFERENCES mlops_job(id) ON DELETE CASCADE
);

-- Indexes for query performance
CREATE INDEX IF NOT EXISTS idx_mlops_job_node_summary_eval_overall_score
    ON mlops_job_node_summary_eval(overall_score);

COMMENT ON TABLE mlops_job_node_summary_eval IS 'Summary node evaluation results - summary quality metrics';
COMMENT ON COLUMN mlops_job_node_summary_eval.id IS 'Shared primary key with mlops_job (one-to-one relationship)';
COMMENT ON COLUMN mlops_job_node_summary_eval.overall_score IS 'Overall quality score (0.0-1.0)';
COMMENT ON COLUMN mlops_job_node_summary_eval.semantic_similarity IS 'Semantic similarity score (0.0-1.0)';
COMMENT ON COLUMN mlops_job_node_summary_eval.factual_accuracy IS 'Factual accuracy score (0.0-1.0)';
COMMENT ON COLUMN mlops_job_node_summary_eval.conciseness IS 'Conciseness score (0.0-1.0)';
COMMENT ON COLUMN mlops_job_node_summary_eval.professional_tone IS 'Professional tone score (0.0-1.0)';
COMMENT ON COLUMN mlops_job_node_summary_eval.total_tokens IS 'Total tokens used by summary node';
COMMENT ON COLUMN mlops_job_node_summary_eval.llm_call_count IS 'Number of LLM calls made by summary node';