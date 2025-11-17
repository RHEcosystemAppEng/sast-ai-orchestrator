-- Migration to create MLOps issue-level details table
-- Version: V007 - Create mlops_issue_details table for individual issue tracking

-- Create mlops_issue_details table
CREATE TABLE IF NOT EXISTS mlops_issue_details (
    id BIGSERIAL PRIMARY KEY,
    mlops_job_id BIGINT NOT NULL REFERENCES mlops_job(id) ON DELETE CASCADE,

    -- Issue Identification (from Excel "AI report" sheet)
    issue_id VARCHAR(50) NOT NULL,           -- e.g., def1, def2
    issue_name VARCHAR(255),                 -- e.g., OVERRUN, BUFFER_SIZE
    error_description TEXT,                  -- Full error details with file:line info

    -- AI Investigation Result
    investigation_result VARCHAR(20),        -- 'TRUE POSITIVE', 'FALSE POSITIVE', 'TRUE NEGATIVE', 'FALSE NEGATIVE'

    -- AI Analysis Details
    hint TEXT,                               -- Brief hint about the issue
    justifications TEXT,                     -- AI's reasoning for the classification
    recommendations TEXT,                    -- AI's recommendations

    -- Metrics
    answer_relevancy NUMERIC(5, 4),         -- Answer relevancy score (0.0 to 1.0)

    -- Context (optional, can be large)
    context TEXT,                            -- Source code context and examples

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_mlops_issue_mlops_job_id ON mlops_issue_details(mlops_job_id);
CREATE INDEX IF NOT EXISTS idx_mlops_issue_result ON mlops_issue_details(investigation_result);
CREATE INDEX IF NOT EXISTS idx_mlops_issue_name ON mlops_issue_details(issue_name);
CREATE INDEX IF NOT EXISTS idx_mlops_issue_id ON mlops_issue_details(issue_id);

-- Comments for documentation
COMMENT ON TABLE mlops_issue_details IS 'Individual issue-level details for MLOps job executions from AI analysis reports';
COMMENT ON COLUMN mlops_issue_details.investigation_result IS 'AI classification: TRUE POSITIVE, FALSE POSITIVE, TRUE NEGATIVE, or FALSE NEGATIVE';
COMMENT ON COLUMN mlops_issue_details.answer_relevancy IS 'AI answer relevancy score (0.0 to 1.0)';