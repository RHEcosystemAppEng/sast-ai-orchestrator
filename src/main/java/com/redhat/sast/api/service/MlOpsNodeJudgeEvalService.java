package com.redhat.sast.api.service;

import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobNodeJudgeEval;
import com.redhat.sast.api.repository.MlOpsJobNodeJudgeEvalRepository;

import io.fabric8.tekton.v1.PipelineRun;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for extracting and storing judge LLM node evaluation results from Tekton pipeline runs.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MlOpsNodeJudgeEvalService {

    private static final String JUDGE_EVAL_RESULTS_KEY = "judge-evaluation-results";

    private final MlOpsJobNodeJudgeEvalRepository judgeEvalRepository;
    private final MlOpsJobService mlOpsJobService;
    private final MlOpsMetricsService mlOpsMetricsService;
    private final ObjectMapper objectMapper;

    /**
     * Extracts judge node evaluation results from PipelineRun and saves to database.
     * Gracefully handles missing results (when judge evaluation is not run).
     */
    @Transactional
    public void extractAndSaveJudgeEval(Long jobId, PipelineRun pipelineRun) {
        try {
            MlOpsJob job = mlOpsJobService.getJobEntityById(jobId);
            if (job == null) {
                LOGGER.warn("MLOps job {} not found, cannot save judge evaluation", jobId);
                return;
            }

            // Extract judge-evaluation-results from Tekton
            String judgeResultsJson = mlOpsMetricsService.extractTektonResult(pipelineRun, JUDGE_EVAL_RESULTS_KEY);
            if (judgeResultsJson == null || judgeResultsJson.isBlank() || "{}".equals(judgeResultsJson.trim())) {
                LOGGER.debug("No {} found for job {} - judge evaluation not run", JUDGE_EVAL_RESULTS_KEY, jobId);
                return; // Gracefully skip - judge evaluation is optional
            }

            // Parse and save judge evaluation
            MlOpsJobNodeJudgeEval judgeEval = parseJudgeEval(judgeResultsJson, job);
            if (judgeEval != null) {
                judgeEvalRepository.persist(judgeEval);
                LOGGER.info(
                        "Successfully saved judge evaluation for MLOps job {}: overall_score={}, tokens={}, llm_calls={}",
                        jobId,
                        judgeEval.getOverallScore(),
                        judgeEval.getTotalTokens(),
                        judgeEval.getLlmCallCount());
            } else {
                LOGGER.warn("Could not parse judge-evaluation-results for job {} - invalid JSON format", jobId);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to extract and save judge evaluation for MLOps job {}: {}", jobId, e.getMessage(), e);
            // Don't rethrow - judge evaluation failure shouldn't break job completion
        }
    }

    /**
     * Parses judge evaluation JSON and creates MlOpsJobNodeJudgeEval entity.
     * Format: {"quality": {"overall": 0.85, "clarity": 0.80, ...}, "perf": {...}}
     */
    private MlOpsJobNodeJudgeEval parseJudgeEval(String judgeResultsJson, MlOpsJob job) {
        try {
            JsonNode root = objectMapper.readTree(judgeResultsJson);

            MlOpsJobNodeJudgeEval judgeEval = new MlOpsJobNodeJudgeEval();
            judgeEval.setMlOpsJob(job);

            // Extract quality metrics
            JsonNode quality = root.path("quality");
            if (!quality.isMissingNode()) {
                judgeEval.setOverallScore(extractBigDecimal(quality, "overall"));
                judgeEval.setClarity(extractBigDecimal(quality, "clarity"));
                judgeEval.setCompleteness(extractBigDecimal(quality, "completeness"));
                judgeEval.setTechnicalAccuracy(extractBigDecimal(quality, "tech_accuracy"));
                judgeEval.setLogicalFlow(extractBigDecimal(quality, "logical_flow"));
            }

            // Extract performance metrics (asInt handles missing nodes with default 0)
            JsonNode perf = root.path("perf");
            judgeEval.setTotalTokens(perf.path("total_tokens").asInt(0));
            judgeEval.setLlmCallCount(perf.path("llm_calls").asInt(0));

            LOGGER.debug(
                    "Parsed judge evaluation for job {}: overall={}, clarity={}, completeness={}, tech_accuracy={}, logical_flow={}",
                    job.getId(),
                    judgeEval.getOverallScore(),
                    judgeEval.getClarity(),
                    judgeEval.getCompleteness(),
                    judgeEval.getTechnicalAccuracy(),
                    judgeEval.getLogicalFlow());

            return judgeEval;

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse judge evaluation JSON for job {}: {}", job.getId(), e.getMessage(), e);
            LOGGER.debug("Problematic JSON: {}", judgeResultsJson);
            return null;
        }
    }

    /**
     * Extracts BigDecimal from JSON node, handling null values.
     * Uses decimalValue() for precise decimal conversion without double precision loss.
     */
    private BigDecimal extractBigDecimal(JsonNode parentNode, String fieldName) {
        JsonNode fieldNode = parentNode.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull() || !fieldNode.isNumber()) {
            if (!fieldNode.isMissingNode() && !fieldNode.isNull() && !fieldNode.isNumber()) {
                LOGGER.warn(
                        "Invalid BigDecimal value for field '{}': {}. Defaulting to null.",
                        fieldName,
                        fieldNode.asText());
            }
            return null;
        }
        try {
            return fieldNode.decimalValue();
        } catch (NumberFormatException e) {
            LOGGER.warn("Failed to parse {} as BigDecimal: {}. Defaulting to null.", fieldName, e.getMessage());
            return null;
        }
    }
}
