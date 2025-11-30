-- Migration to create mlops_job_issue table
-- Version: V009 - Add mlops_job_issue table for storing AI analysis results from Excel reports

-- Create mlops_job_issue table
CREATE TABLE IF NOT EXISTS mlops_job_issue (
    id BIGSERIAL PRIMARY KEY,
    mlops_job_id BIGINT NOT NULL,
    issue_id VARCHAR(50) NOT NULL,
    issue_name VARCHAR(100),
    investigation_result VARCHAR(50),
    hint TEXT,
    answer_relevancy VARCHAR(20),
    s3_file_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mlops_job_issue_mlops_job
        FOREIGN KEY (mlops_job_id) REFERENCES mlops_job(id) ON DELETE CASCADE
);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_mlops_job_issue_mlops_job_id ON mlops_job_issue(mlops_job_id);
CREATE INDEX IF NOT EXISTS idx_mlops_job_issue_result ON mlops_job_issue(investigation_result);

-- Comments for documentation
COMMENT ON TABLE mlops_job_issue IS 'Stores AI analysis results (issues) from Excel report per MLOps job';
COMMENT ON COLUMN mlops_job_issue.mlops_job_id IS 'Foreign key to mlops_job (one-to-many relationship)';
COMMENT ON COLUMN mlops_job_issue.issue_id IS 'Issue identifier from Excel (e.g., def1, def2)';
COMMENT ON COLUMN mlops_job_issue.issue_name IS 'Type of issue (e.g., RESOURCE_LEAK, UNINIT)';
COMMENT ON COLUMN mlops_job_issue.investigation_result IS 'AI classification result (TRUE POSITIVE, FALSE POSITIVE, etc.)';
COMMENT ON COLUMN mlops_job_issue.hint IS 'Brief hint about the issue';
COMMENT ON COLUMN mlops_job_issue.answer_relevancy IS 'Relevancy score as percentage';
COMMENT ON COLUMN mlops_job_issue.s3_file_url IS 'URL/path to the Excel file in S3/MinIO';