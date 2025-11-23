-- Migration to create mlops_job_settings table
-- Version: V007 - Add mlops_job_settings table for storing MLOps job configuration

-- Create mlops_job_settings table
CREATE TABLE IF NOT EXISTS mlops_job_settings (
    id BIGSERIAL PRIMARY KEY,
    mlops_job_id BIGINT UNIQUE,
    llm_url VARCHAR(500),
    llm_model_name VARCHAR(255),
    embedding_llm_url VARCHAR(500),
    embedding_llm_model_name VARCHAR(255),
    secret_name VARCHAR(255),
    use_known_false_positive_file BOOLEAN,
    CONSTRAINT fk_mlops_job_settings_mlops_job
        FOREIGN KEY (mlops_job_id) REFERENCES mlops_job(id) ON DELETE CASCADE
);

-- Create index as defined in the entity
CREATE INDEX IF NOT EXISTS idx_mlops_job_settings_id ON mlops_job_settings(id);

-- Comments for documentation
COMMENT ON TABLE mlops_job_settings IS 'Stores configurable settings for MLOps jobs including LLM configurations';
COMMENT ON COLUMN mlops_job_settings.mlops_job_id IS 'Foreign key to mlops_job (one-to-one relationship)';
COMMENT ON COLUMN mlops_job_settings.llm_url IS 'URL for the primary LLM service';
COMMENT ON COLUMN mlops_job_settings.embedding_llm_url IS 'URL for the embedding LLM service';
