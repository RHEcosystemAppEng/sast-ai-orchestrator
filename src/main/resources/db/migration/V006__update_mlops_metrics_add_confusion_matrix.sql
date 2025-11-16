-- Migration to add confusion matrix fields to MLOps job metrics
-- Version: V006 - Add confusion matrix fields

-- Add confusion matrix fields
ALTER TABLE mlops_job_metrics ADD COLUMN IF NOT EXISTS cm_tp INTEGER;
ALTER TABLE mlops_job_metrics ADD COLUMN IF NOT EXISTS cm_fp INTEGER;
ALTER TABLE mlops_job_metrics ADD COLUMN IF NOT EXISTS cm_tn INTEGER;
ALTER TABLE mlops_job_metrics ADD COLUMN IF NOT EXISTS cm_fn INTEGER;

-- Remove has_ground_truth since it's not in the new format
ALTER TABLE mlops_job_metrics DROP COLUMN IF EXISTS has_ground_truth;

-- Comments for documentation
COMMENT ON COLUMN mlops_job_metrics.cm_tp IS 'Confusion matrix: True Positives';
COMMENT ON COLUMN mlops_job_metrics.cm_fp IS 'Confusion matrix: False Positives';
COMMENT ON COLUMN mlops_job_metrics.cm_tn IS 'Confusion matrix: True Negatives';
COMMENT ON COLUMN mlops_job_metrics.cm_fn IS 'Confusion matrix: False Negatives';

