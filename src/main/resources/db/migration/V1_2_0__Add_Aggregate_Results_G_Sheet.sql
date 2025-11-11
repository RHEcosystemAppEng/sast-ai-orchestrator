-- Add aggregate_results_g_sheet column to job_batch table
ALTER TABLE job_batch ADD COLUMN aggregate_results_g_sheet VARCHAR(500);

-- Add aggregate_results_g_sheet column to job table
ALTER TABLE job ADD COLUMN aggregate_results_g_sheet VARCHAR(500);
