-- Migration: Add evaluate_specific_node column to mlops_job_settings
-- Description: Stores which evaluation nodes to run (all, filter, judge, summary, or combinations)
-- This helps filter dashboards by evaluation mode

ALTER TABLE mlops_job_settings
ADD COLUMN IF NOT EXISTS evaluate_specific_node VARCHAR(100);

COMMENT ON COLUMN mlops_job_settings.evaluate_specific_node IS 'Comma-separated list of evaluation nodes to run (e.g., "all", "filter", "judge,summary")';