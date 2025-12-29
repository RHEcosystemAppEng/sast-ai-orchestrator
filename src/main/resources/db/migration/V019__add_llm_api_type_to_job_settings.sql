-- Add llm_api_type column to mlops_job_settings table
-- This allows users to override the LLM API type (e.g., 'openai', 'nim', 'azure') from the DTO
ALTER TABLE mlops_job_settings
    ADD COLUMN IF NOT EXISTS llm_api_type VARCHAR(50);

-- Add llm_api_type column to job_settings table for consistency
ALTER TABLE job_settings
ADD COLUMN IF NOT EXISTS llm_api_type VARCHAR(50);

-- Add column comments for schema documentation
COMMENT ON COLUMN mlops_job_settings.llm_api_type IS 'LLM API provider type override (e.g., openai, nim, azure). Falls back to secret value if not specified.';
COMMENT ON COLUMN job_settings.llm_api_type IS 'LLM API provider type override (e.g., openai, nim, azure). Falls back to secret value if not specified.';