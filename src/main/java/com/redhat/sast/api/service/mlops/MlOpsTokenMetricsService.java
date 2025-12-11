package com.redhat.sast.api.service.mlops;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobTokenUsage;
import com.redhat.sast.api.repository.mlops.MlOpsJobTokenUsageRepository;
import com.redhat.sast.api.service.S3ClientService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for extracting and storing MLOps job token usage metrics from S3.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MlOpsTokenMetricsService {

    private final MlOpsJobTokenUsageRepository tokenUsageRepository;
    private final MlOpsJobService mlOpsJobService;
    private final S3ClientService s3ClientService;
    private final ObjectMapper objectMapper;

    /**
     * Downloads token usage metrics from S3 and saves to database.
     * Format from sast-ai-workflow: [{tool_name, model, input_tokens, output_tokens, total_tokens, duration_seconds}]
     */
    @Transactional
    public void fetchAndSaveTokenMetrics(Long jobId, String pipelineRunId) {
        try {
            MlOpsJob job = mlOpsJobService.getJobEntityById(jobId);
            if (job == null) {
                LOGGER.warn("MLOps job {} not found, cannot save token metrics", jobId);
                return;
            }

            // Construct S3 key
            String s3Key = s3ClientService.constructTokenUsageS3Key(
                    job.getPackageName(), job.getProjectVersion(), pipelineRunId);

            LOGGER.debug("Fetching token metrics from S3 for job {}: {}", jobId, s3Key);

            // Download from S3
            String tokenMetricsJson = s3ClientService.downloadFileAsString(s3Key);
            if (tokenMetricsJson == null || tokenMetricsJson.isBlank()) {
                LOGGER.info(
                        "No token metrics found in S3 for job {} - file may not have been uploaded yet or workflow didn't use LLM",
                        jobId);
                return;
            }

            // Parse and save metrics
            MlOpsJobTokenUsage tokenUsage = parseTokenMetrics(tokenMetricsJson, job);
            if (tokenUsage != null) {
                tokenUsageRepository.persist(tokenUsage);
                LOGGER.info(
                        "Successfully saved token metrics for MLOps job {}: total_tokens={}, total_duration={}s",
                        jobId,
                        tokenUsage.getTotalTokens(),
                        tokenUsage.getTotalDurationSeconds());
            } else {
                LOGGER.warn("Could not parse token metrics for job {} - invalid JSON format", jobId);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to fetch and save token metrics for MLOps job {}: {}", jobId, e.getMessage(), e);
            // Don't rethrow - token metrics are optional, shouldn't break job completion
        }
    }

    /**
     * Parses token metrics JSON and creates MlOpsJobTokenUsage entity.
     * Expected format: [{tool_name, model, input_tokens, output_tokens, total_tokens, duration_seconds}]
     */
    private MlOpsJobTokenUsage parseTokenMetrics(String tokenMetricsJson, MlOpsJob job) {
        try {
            JsonNode metricsArray = objectMapper.readTree(tokenMetricsJson);

            if (!metricsArray.isArray()) {
                LOGGER.warn("Token metrics JSON is not an array for job {}", job.getId());
                return null;
            }

            MlOpsJobTokenUsage tokenUsage = new MlOpsJobTokenUsage();
            tokenUsage.setMlOpsJob(job);
            tokenUsage.setNodeBreakdown(tokenMetricsJson); // Store raw JSONB

            // Aggregate totals
            int totalInputTokens = 0;
            int totalOutputTokens = 0;
            int totalTokens = 0;
            double totalDuration = 0.0;

            for (JsonNode node : metricsArray) {
                totalInputTokens += extractInteger(node, "input_tokens");
                totalOutputTokens += extractInteger(node, "output_tokens");
                totalTokens += extractInteger(node, "total_tokens");

                // Duration may be null for some nodes
                JsonNode durationNode = node.path("duration_seconds");
                if (!durationNode.isMissingNode() && !durationNode.isNull()) {
                    totalDuration += durationNode.asDouble();
                }
            }

            tokenUsage.setTotalInputTokens(totalInputTokens);
            tokenUsage.setTotalOutputTokens(totalOutputTokens);
            tokenUsage.setTotalTokens(totalTokens);
            tokenUsage.setTotalDurationSeconds(BigDecimal.valueOf(totalDuration));

            LOGGER.debug(
                    "Parsed token metrics for job {}: input={}, output={}, total={}, duration={}s, nodes={}",
                    job.getId(),
                    totalInputTokens,
                    totalOutputTokens,
                    totalTokens,
                    totalDuration,
                    metricsArray.size());

            return tokenUsage;

        } catch (Exception e) {
            LOGGER.error("Failed to parse token metrics JSON for job {}: {}", job.getId(), e.getMessage(), e);
            LOGGER.debug("Problematic JSON: {}", tokenMetricsJson);
            return null;
        }
    }

    /**
     * Extracts Integer from JSON node, handling null values.
     */
    private Integer extractInteger(JsonNode parentNode, String fieldName) {
        JsonNode fieldNode = parentNode.path(fieldName);
        if (fieldNode.isNull() || fieldNode.isMissingNode()) {
            return 0;
        }
        try {
            return fieldNode.asInt();
        } catch (Exception e) {
            LOGGER.warn("Failed to parse {} as Integer: {}", fieldName, e.getMessage());
            return 0;
        }
    }
}
