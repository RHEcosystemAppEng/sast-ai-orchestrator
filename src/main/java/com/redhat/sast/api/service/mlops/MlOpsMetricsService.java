package com.redhat.sast.api.service.mlops;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobMetrics;
import com.redhat.sast.api.repository.mlops.MlOpsJobMetricsRepository;

import io.fabric8.tekton.v1.ParamValue;
import io.fabric8.tekton.v1.PipelineRun;
import io.fabric8.tekton.v1.PipelineRunResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for extracting and storing MLOps job metrics from pipeline results.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MlOpsMetricsService {

    private final MlOpsJobMetricsRepository mlOpsJobMetricsRepository;
    private final MlOpsJobService mlOpsJobService;
    private final ObjectMapper objectMapper;

    /**
     * Extracts workflow metrics from PipelineRun results and saves to database.
     * Gracefully handles missing metrics (when pipeline fails or doesn't produce metrics).
     */
    @Transactional
    public void extractAndSaveMetrics(Long jobId, PipelineRun pipelineRun) {
        try {
            MlOpsJob job = mlOpsJobService.getJobEntityById(jobId);
            if (job == null) {
                LOGGER.warn("MLOps job {} not found, cannot save metrics", jobId);
                return;
            }

            // Extract workflow-metrics result
            String workflowMetricsJson = extractWorkflowMetrics(pipelineRun);
            if (workflowMetricsJson == null || workflowMetricsJson.isBlank()) {
                LOGGER.info(
                        "No workflow-metrics found in PipelineRun for job {} - pipeline may have failed or not produced metrics",
                        jobId);
                return; // Gracefully skip - this is normal for failed pipelines
            }

            // Parse and save metrics
            MlOpsJobMetrics metrics = parseMetrics(workflowMetricsJson, job);
            if (metrics != null) {
                mlOpsJobMetricsRepository.persist(metrics);
                LOGGER.info(
                        "Successfully saved metrics for MLOps job {}: accuracy={}, precision={}, recall={}, f1={}, CM(tp={}, fp={}, tn={}, fn={})",
                        jobId,
                        metrics.getAccuracy(),
                        metrics.getPrecision(),
                        metrics.getRecall(),
                        metrics.getF1Score(),
                        metrics.getCmTp(),
                        metrics.getCmFp(),
                        metrics.getCmTn(),
                        metrics.getCmFn());
            } else {
                LOGGER.warn("Could not parse workflow-metrics for job {} - invalid JSON format", jobId);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to extract and save metrics for MLOps job {}: {}", jobId, e.getMessage(), e);
            // Don't rethrow - metrics extraction failure shouldn't break job completion
        }
    }

    /**
     * Extracts a specific Tekton result by name from PipelineRun.
     */
    public String extractTektonResult(PipelineRun pipelineRun, String resultName) {
        if (pipelineRun.getStatus() == null || pipelineRun.getStatus().getResults() == null) {
            return null;
        }

        for (PipelineRunResult result : pipelineRun.getStatus().getResults()) {
            if (resultName.equals(result.getName())) {
                ParamValue value = result.getValue();
                if (value != null) {
                    return value.getStringVal();
                }
            }
        }

        return null;
    }

    /**
     * Extracts workflow-metrics result from PipelineRun.
     */
    private String extractWorkflowMetrics(PipelineRun pipelineRun) {
        return extractTektonResult(pipelineRun, "workflow-metrics");
    }

    /**
     * Parses workflow metrics JSON and creates MlOpsJobMetrics entity.
     * Format: {"aggregated_metrics":{"accuracy":0.0,"precision":0.0,"recall":0.0,"f1_score":0.0},"cm":{"tp":0,"fp":0,"tn":0,"fn":1}}
     */
    private MlOpsJobMetrics parseMetrics(String workflowMetricsJson, MlOpsJob job) {
        try {
            JsonNode root = objectMapper.readTree(workflowMetricsJson);

            // Check if aggregated_metrics exists
            JsonNode aggregatedMetrics = root.path("aggregated_metrics");
            if (aggregatedMetrics.isMissingNode()) {
                LOGGER.warn("No 'aggregated_metrics' found in workflow-metrics JSON for job {}", job.getId());
                return null;
            }

            MlOpsJobMetrics metrics = new MlOpsJobMetrics();
            metrics.setMlOpsJob(job);
            metrics.setPackageName(job.getPackageName());

            // Extract aggregated metrics
            metrics.setAccuracy(extractBigDecimal(aggregatedMetrics, "accuracy"));
            metrics.setPrecision(extractBigDecimal(aggregatedMetrics, "precision"));
            metrics.setRecall(extractBigDecimal(aggregatedMetrics, "recall"));
            metrics.setF1Score(extractBigDecimal(aggregatedMetrics, "f1_score"));

            // Extract confusion matrix (cm)
            JsonNode cm = root.path("cm");
            if (!cm.isMissingNode()) {
                metrics.setCmTp(extractInteger(cm, "tp"));
                metrics.setCmFp(extractInteger(cm, "fp"));
                metrics.setCmTn(extractInteger(cm, "tn"));
                metrics.setCmFn(extractInteger(cm, "fn"));

                LOGGER.debug(
                        "Parsed metrics for job {}: accuracy={}, precision={}, recall={}, f1_score={}, CM(tp={}, fp={}, tn={}, fn={})",
                        job.getId(),
                        metrics.getAccuracy(),
                        metrics.getPrecision(),
                        metrics.getRecall(),
                        metrics.getF1Score(),
                        metrics.getCmTp(),
                        metrics.getCmFp(),
                        metrics.getCmTn(),
                        metrics.getCmFn());
            } else {
                LOGGER.debug(
                        "Parsed metrics for job {}: accuracy={}, precision={}, recall={}, f1_score={} (no confusion matrix)",
                        job.getId(),
                        metrics.getAccuracy(),
                        metrics.getPrecision(),
                        metrics.getRecall(),
                        metrics.getF1Score());
            }

            return metrics;

        } catch (Exception e) {
            LOGGER.error("Failed to parse workflow metrics JSON for job {}: {}", job.getId(), e.getMessage(), e);
            LOGGER.debug("Problematic JSON: {}", workflowMetricsJson);
            return null;
        }
    }

    /**
     * Extracts BigDecimal from JSON node, handling null values.
     */
    private BigDecimal extractBigDecimal(JsonNode parentNode, String fieldName) {
        JsonNode fieldNode = parentNode.path(fieldName);
        if (fieldNode.isNull() || fieldNode.isMissingNode()) {
            return null;
        }
        try {
            return BigDecimal.valueOf(fieldNode.asDouble());
        } catch (Exception e) {
            LOGGER.warn("Failed to parse {} as BigDecimal: {}", fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts Integer from JSON node, handling null values.
     */
    private Integer extractInteger(JsonNode parentNode, String fieldName) {
        JsonNode fieldNode = parentNode.path(fieldName);
        if (fieldNode.isNull() || fieldNode.isMissingNode()) {
            return null;
        }
        try {
            return fieldNode.asInt();
        } catch (Exception e) {
            LOGGER.warn("Failed to parse {} as Integer: {}", fieldName, e.getMessage());
            return null;
        }
    }
}
