-- Add llm_api_type column to mlops_job_settings table
-- This allows users to override the LLM API type (e.g., 'openai', 'nim', 'azure') from the DTO
ALTER TABLE mlops_job_settings
ADD COLUMN llm_api_type VARCHAR(50);

-- Add llm_api_type column to job_settings table for consistency
ALTER TABLE job_settings
ADD COLUMN llm_api_type VARCHAR(50);