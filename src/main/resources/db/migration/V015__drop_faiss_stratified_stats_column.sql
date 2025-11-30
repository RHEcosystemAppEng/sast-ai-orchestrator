-- Migration: Drop faiss_stratified_stats JSONB column
-- Description: Remove the faiss_stratified_stats JSONB column since we've expanded it to individual columns

ALTER TABLE mlops_job_node_filter_eval
DROP COLUMN faiss_stratified_stats;