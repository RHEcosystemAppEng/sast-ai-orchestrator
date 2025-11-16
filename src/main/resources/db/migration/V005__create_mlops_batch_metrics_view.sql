-- Migration to create aggregated batch-level metrics view for MLOps tables
-- Version: V005 - Create mlops_batch_metrics_view for Grafana dashboards

-- Drop view if it exists (for idempotency)
DROP VIEW IF EXISTS mlops_batch_metrics_view;

-- Create aggregated view for batch-level metrics
CREATE OR REPLACE VIEW mlops_batch_metrics_view AS
SELECT
    -- Batch Information
    b.id AS batch_id,
    b.testing_data_nvrs_version,
    b.prompts_version,
    b.known_non_issues_version,
    b.container_image,
    b.submitted_by,
    b.submitted_at,
    b.status AS batch_status,
    b.total_jobs,
    b.completed_jobs,
    b.failed_jobs,
    b.last_updated_at AS batch_last_updated_at,

    -- Job-level aggregations
    COUNT(DISTINCT jm.mlops_job_id) AS jobs_with_metrics,

    -- Average metrics across all jobs in batch
    ROUND(AVG(jm.accuracy), 4) AS avg_accuracy,
    ROUND(AVG(jm.precision), 4) AS avg_precision,
    ROUND(AVG(jm.recall), 4) AS avg_recall,
    ROUND(AVG(jm.f1_score), 4) AS avg_f1_score,

    -- Confusion matrix totals (summed across all jobs)
    SUM(jm.cm_tp) AS total_tp,
    SUM(jm.cm_fp) AS total_fp,
    SUM(jm.cm_tn) AS total_tn,
    SUM(jm.cm_fn) AS total_fn,

    -- Recalculated batch-level metrics from aggregated confusion matrix
    -- Accuracy = (TP + TN) / (TP + FP + TN + FN)
    CASE
        WHEN (SUM(jm.cm_tp) + SUM(jm.cm_fp) + SUM(jm.cm_tn) + SUM(jm.cm_fn)) > 0
        THEN ROUND(
            (SUM(jm.cm_tp)::NUMERIC + SUM(jm.cm_tn)::NUMERIC) /
            (SUM(jm.cm_tp) + SUM(jm.cm_fp) + SUM(jm.cm_tn) + SUM(jm.cm_fn))::NUMERIC,
            4
        )
        ELSE NULL
    END AS batch_accuracy,

    -- Precision = TP / (TP + FP)
    CASE
        WHEN (SUM(jm.cm_tp) + SUM(jm.cm_fp)) > 0
        THEN ROUND(SUM(jm.cm_tp)::NUMERIC / (SUM(jm.cm_tp) + SUM(jm.cm_fp))::NUMERIC, 4)
        ELSE NULL
    END AS batch_precision,

    -- Recall = TP / (TP + FN)
    CASE
        WHEN (SUM(jm.cm_tp) + SUM(jm.cm_fn)) > 0
        THEN ROUND(SUM(jm.cm_tp)::NUMERIC / (SUM(jm.cm_tp) + SUM(jm.cm_fn))::NUMERIC, 4)
        ELSE NULL
    END AS batch_recall,

    -- F1 Score = 2 * (Precision * Recall) / (Precision + Recall)
    CASE
        WHEN (SUM(jm.cm_tp) + SUM(jm.cm_fp)) > 0 AND (SUM(jm.cm_tp) + SUM(jm.cm_fn)) > 0
        THEN ROUND(
            2.0 * (
                (SUM(jm.cm_tp)::NUMERIC / (SUM(jm.cm_tp) + SUM(jm.cm_fp))::NUMERIC) *
                (SUM(jm.cm_tp)::NUMERIC / (SUM(jm.cm_tp) + SUM(jm.cm_fn))::NUMERIC)
            ) / (
                (SUM(jm.cm_tp)::NUMERIC / (SUM(jm.cm_tp) + SUM(jm.cm_fp))::NUMERIC) +
                (SUM(jm.cm_tp)::NUMERIC / (SUM(jm.cm_tp) + SUM(jm.cm_fn))::NUMERIC)
            ),
            4
        )
        ELSE NULL
    END AS batch_f1_score,

    -- Timestamps
    MIN(jm.created_at) AS first_metric_at,
    MAX(jm.created_at) AS last_metric_at,
    MIN(j.started_at) AS first_job_started_at,
    MAX(j.completed_at) AS last_job_completed_at

FROM mlops_batch b
LEFT JOIN mlops_job j ON b.id = j.mlops_batch_id
LEFT JOIN mlops_job_metrics jm ON j.id = jm.mlops_job_id
GROUP BY
    b.id,
    b.testing_data_nvrs_version,
    b.prompts_version,
    b.known_non_issues_version,
    b.container_image,
    b.submitted_by,
    b.submitted_at,
    b.status,
    b.total_jobs,
    b.completed_jobs,
    b.failed_jobs,
    b.last_updated_at;

-- Comments for documentation
COMMENT ON VIEW mlops_batch_metrics_view IS 'Aggregated MLOps batch metrics view for Grafana dashboards - provides batch-level analytics by aggregating mlops_job_metrics data';