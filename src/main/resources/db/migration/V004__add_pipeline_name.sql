-- Add pipeline name tracking and MLOps-specific fields
-- Version: V004 - Add pipeline name and MLOps parameters

-- Add pipeline_name column to job table
ALTER TABLE job ADD COLUMN pipeline_name VARCHAR(100);
UPDATE job SET pipeline_name = 'sast-ai-workflow-pipeline' WHERE pipeline_name IS NULL;
ALTER TABLE job ALTER COLUMN pipeline_name SET DEFAULT 'sast-ai-workflow-pipeline';

-- Add pipeline_name column to job_batch table
ALTER TABLE job_batch ADD COLUMN pipeline_name VARCHAR(100);
UPDATE job_batch SET pipeline_name = 'sast-ai-workflow-pipeline' WHERE pipeline_name IS NULL;
ALTER TABLE job_batch ALTER COLUMN pipeline_name SET DEFAULT 'sast-ai-workflow-pipeline';

-- Add MLOps-specific fields to job_settings table
ALTER TABLE job_settings ADD COLUMN dvc_nvr_version VARCHAR(255);
ALTER TABLE job_settings ADD COLUMN dvc_known_false_positives_version VARCHAR(255);
ALTER TABLE job_settings ADD COLUMN dvc_prompts_version VARCHAR(255);
ALTER TABLE job_settings ADD COLUMN image_version VARCHAR(255);

-- Make batch_google_sheet_url nullable for MLOps batches
-- MLOps batches don't use Google Sheets; package lists come from DVC
ALTER TABLE job_batch ALTER COLUMN batch_google_sheet_url DROP NOT NULL;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_job_pipeline_name ON job(pipeline_name);
CREATE INDEX IF NOT EXISTS idx_job_batch_pipeline_name ON job_batch(pipeline_name);

-- Add comments for documentation
COMMENT ON COLUMN job.pipeline_name IS 'Tekton pipeline name (e.g., sast-ai-workflow-pipeline, sast-ai-workflow-pipeline-mlops)';
COMMENT ON COLUMN job_batch.pipeline_name IS 'Tekton pipeline name (e.g., sast-ai-workflow-pipeline, sast-ai-workflow-pipeline-mlops)';
COMMENT ON COLUMN job_settings.dvc_nvr_version IS 'DVC NVR version for MLOps pipeline';
COMMENT ON COLUMN job_settings.dvc_known_false_positives_version IS 'DVC known false positives version for MLOps pipeline';
COMMENT ON COLUMN job_settings.dvc_prompts_version IS 'DVC prompts version for MLOps pipeline';
COMMENT ON COLUMN job_settings.image_version IS 'Container image version for MLOps pipeline (defaults to latest)';

