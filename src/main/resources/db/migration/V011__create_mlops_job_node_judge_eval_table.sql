-- Migration: Create mlops_job_node_judge_eval table
-- Description: Stores judge LLM node evaluation results from Tekton pipeline runs
-- Judge node evaluates quality of LLM-generated justifications/analysis

CREATE TABLE IF NOT EXISTS mlops_job_node_judge_eval (
    id BIGINT PRIMARY KEY,
    overall_score DECIMAL(5, 4),
    clarity DECIMAL(5, 4),
    completeness DECIMAL(5, 4),
    technical_accuracy DECIMAL(5, 4),
    logical_flow DECIMAL(5, 4),
    total_tokens INTEGER DEFAULT 0,
    llm_call_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mlops_job_node_judge_eval_mlops_job
        FOREIGN KEY (id) REFERENCES mlops_job(id) ON DELETE CASCADE
);

-- Indexes for query performance
CREATE INDEX IF NOT EXISTS idx_mlops_job_node_judge_eval_overall_score
    ON mlops_job_node_judge_eval(overall_score);

COMMENT ON TABLE mlops_job_node_judge_eval IS 'Judge LLM node evaluation results - justification quality metrics';
COMMENT ON COLUMN mlops_job_node_judge_eval.id IS 'Shared primary key with mlops_job (one-to-one relationship)';
COMMENT ON COLUMN mlops_job_node_judge_eval.overall_score IS 'Overall quality score (0.0-1.0)';
COMMENT ON COLUMN mlops_job_node_judge_eval.clarity IS 'Clarity score (0.0-1.0)';
COMMENT ON COLUMN mlops_job_node_judge_eval.completeness IS 'Completeness score (0.0-1.0)';
COMMENT ON COLUMN mlops_job_node_judge_eval.technical_accuracy IS 'Technical accuracy score (0.0-1.0)';
COMMENT ON COLUMN mlops_job_node_judge_eval.logical_flow IS 'Logical flow score (0.0-1.0)';
COMMENT ON COLUMN mlops_job_node_judge_eval.total_tokens IS 'Total tokens used by judge node';
COMMENT ON COLUMN mlops_job_node_judge_eval.llm_call_count IS 'Number of LLM calls made by judge node';