-- Migration: Fix job_settings table schema to match JobSettings entity
-- Description: Add missing columns and remove obsolete ones
-- Version: V018

-- Add missing columns that the entity expects
ALTER TABLE job_settings ADD COLUMN IF NOT EXISTS llm_url VARCHAR(255);
ALTER TABLE job_settings ADD COLUMN IF NOT EXISTS llm_api_key VARCHAR(255);
ALTER TABLE job_settings ADD COLUMN IF NOT EXISTS embedding_llm_url VARCHAR(255);
ALTER TABLE job_settings ADD COLUMN IF NOT EXISTS embedding_llm_model_name VARCHAR(255);
ALTER TABLE job_settings ADD COLUMN IF NOT EXISTS embedding_llm_api_key VARCHAR(255);
ALTER TABLE job_settings ADD COLUMN IF NOT EXISTS secret_name VARCHAR(255);

-- Drop obsolete columns that are not in the entity
-- Note: These columns were in the original V001 but are no longer used
ALTER TABLE job_settings DROP COLUMN IF EXISTS embeddings_model_name;
ALTER TABLE job_settings DROP COLUMN IF EXISTS calculate_ragas_metrics;
ALTER TABLE job_settings DROP COLUMN IF EXISTS chunk_size;
ALTER TABLE job_settings DROP COLUMN IF EXISTS chunk_overlap;
ALTER TABLE job_settings DROP COLUMN IF EXISTS max_analysis_iterations;
ALTER TABLE job_settings DROP COLUMN IF EXISTS settings_json;

-- Add comments for documentation
COMMENT ON COLUMN job_settings.llm_url IS 'URL for the LLM service';
COMMENT ON COLUMN job_settings.llm_model_name IS 'Model name for LLM';
COMMENT ON COLUMN job_settings.llm_api_key IS 'API key for LLM service';
COMMENT ON COLUMN job_settings.embedding_llm_url IS 'URL for embedding LLM service';
COMMENT ON COLUMN job_settings.embedding_llm_model_name IS 'Model name for embedding LLM';
COMMENT ON COLUMN job_settings.embedding_llm_api_key IS 'API key for embedding LLM service';
COMMENT ON COLUMN job_settings.secret_name IS 'Name of the secret containing credentials';
COMMENT ON COLUMN job_settings.use_known_false_positive_file IS 'Whether to use known false positive file';
