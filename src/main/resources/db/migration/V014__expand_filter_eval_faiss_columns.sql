-- Migration: Expand FAISS stratified stats to individual columns
-- Description: Extract FAISS JSON metrics into individual columns for easier querying in Grafana

-- Add columns for "with_expected_matches" metrics
ALTER TABLE mlops_job_node_filter_eval
ADD COLUMN with_expected_total INTEGER DEFAULT 0,
ADD COLUMN with_expected_faiss_found INTEGER DEFAULT 0,
ADD COLUMN with_expected_perc_correct DECIMAL(5,4) DEFAULT 0.0;

-- Add columns for "without_expected_matches" metrics
ALTER TABLE mlops_job_node_filter_eval
ADD COLUMN without_expected_total INTEGER DEFAULT 0,
ADD COLUMN without_expected_faiss_found INTEGER DEFAULT 0,
ADD COLUMN without_expected_perc_correct DECIMAL(5,4) DEFAULT 0.0;

-- Add comments
COMMENT ON COLUMN mlops_job_node_filter_eval.with_expected_total IS 'Total issues with expected matches';
COMMENT ON COLUMN mlops_job_node_filter_eval.with_expected_faiss_found IS 'Issues with expected matches where FAISS found matches';
COMMENT ON COLUMN mlops_job_node_filter_eval.with_expected_perc_correct IS 'Percentage correct for issues with expected matches';
COMMENT ON COLUMN mlops_job_node_filter_eval.without_expected_total IS 'Total issues without expected matches';
COMMENT ON COLUMN mlops_job_node_filter_eval.without_expected_faiss_found IS 'Issues without expected matches where FAISS found matches';
COMMENT ON COLUMN mlops_job_node_filter_eval.without_expected_perc_correct IS 'Percentage correct for issues without expected matches';