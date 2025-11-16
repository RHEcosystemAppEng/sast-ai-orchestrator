-- Migration to add metrics table for MLOps jobs
-- Version: V005 - Add MLOps job metrics table

CREATE TABLE IF NOT EXISTS mlops_job_metrics (
    id BIGSERIAL PRIMARY KEY,
    mlops_job_id BIGINT NOT NULL REFERENCES mlops_job(id),
    package_name VARCHAR(255) NOT NULL,
    accuracy NUMERIC(5, 4),
    precision NUMERIC(5, 4),
    recall NUMERIC(5, 4),
    f1_score NUMERIC(5, 4),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_mlops_job_metrics FOREIGN KEY (mlops_job_id) REFERENCES mlops_job(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_mlops_job_metrics_id ON mlops_job_metrics(id);
CREATE INDEX IF NOT EXISTS idx_mlops_job_metrics_mlops_job_id ON mlops_job_metrics(mlops_job_id);
CREATE INDEX IF NOT EXISTS idx_mlops_job_metrics_package_name ON mlops_job_metrics(package_name);

-- Comments for documentation
COMMENT ON TABLE mlops_job_metrics IS 'Stores aggregated metrics from MLOps job pipeline results';
COMMENT ON COLUMN mlops_job_metrics.accuracy IS 'Accuracy metric from workflow-metrics result';
COMMENT ON COLUMN mlops_job_metrics.precision IS 'Precision metric from workflow-metrics result';
COMMENT ON COLUMN mlops_job_metrics.recall IS 'Recall metric from workflow-metrics result';
COMMENT ON COLUMN mlops_job_metrics.f1_score IS 'F1 score metric from workflow-metrics result';

