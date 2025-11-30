package com.redhat.sast.api.service;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobNodeSummaryEval;
import com.redhat.sast.api.repository.MlOpsJobNodeSummaryEvalRepository;

import io.fabric8.tekton.v1.PipelineRun;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for extracting and storing summary node evaluation results from Tekton pipeline runs.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MlOpsNodeSummaryEvalService {

    private final MlOpsJobNodeSummaryEvalRepository summaryEvalRepository;
    private final MlOpsJobService mlOpsJobService;
    private final MlOpsMetricsService mlOpsMetricsService;
    private final ObjectMapper objectMapper;

    /**
     * Extracts summary node evaluation results from PipelineRun and saves to database.
     * Gracefully handles missing results (when summary evaluation is not run).
     */
    @Transactional
    public void extractAndSaveSummaryEval(Long jobId, PipelineRun pipelineRun) {
        try {
            MlOpsJob job = mlOpsJobService.getJobEntityById(jobId);
            if (job == null) {
                LOGGER.warn("MLOps job {} not found, cannot save summary evaluation", jobId);
                return;
            }

            // Extract summary-evaluation-results from Tekton
            String summaryResultsJson =
                    mlOpsMetricsService.extractTektonResult(pipelineRun, "summary-evaluation-results");
            if (summaryResultsJson == null || summaryResultsJson.isBlank() || "{}".equals(summaryResultsJson.trim())) {
                LOGGER.debug("No summary-evaluation-results found for job {} - summary evaluation not run", jobId);
                return; // Gracefully skip - summary evaluation is optional
            }

            // Parse and save summary evaluation
            MlOpsJobNodeSummaryEval summaryEval = parseSummaryEval(summaryResultsJson, job);
            if (summaryEval != null) {
                summaryEvalRepository.persist(summaryEval);
                LOGGER.info(
                        "Successfully saved summary evaluation for MLOps job {}: overall_score={}, tokens={}, llm_calls={}",
                        jobId,
                        summaryEval.getOverallScore(),
                        summaryEval.getTotalTokens(),
                        summaryEval.getLlmCallCount());
            } else {
                LOGGER.warn("Could not parse summary-evaluation-results for job {} - invalid JSON format", jobId);
            }

        } catch (Exception e) {
            LOGGER.error(
                    "Failed to extract and save summary evaluation for MLOps job {}: {}", jobId, e.getMessage(), e);
            // Don't rethrow - summary evaluation failure shouldn't break job completion
        }
    }

    /**
     * Parses summary evaluation JSON and creates MlOpsJobNodeSummaryEval entity.
     * Format: {"quality": {"overall": 0.85, "sem_similarity": 0.82, ...}, "perf": {...}}
     */
    private MlOpsJobNodeSummaryEval parseSummaryEval(String summaryResultsJson, MlOpsJob job) {
        try {
            JsonNode root = objectMapper.readTree(summaryResultsJson);

            MlOpsJobNodeSummaryEval summaryEval = new MlOpsJobNodeSummaryEval();
            summaryEval.setMlOpsJob(job);

            // Extract quality metrics
            JsonNode quality = root.path("quality");
            if (!quality.isMissingNode()) {
                summaryEval.setOverallScore(extractBigDecimal(quality, "overall"));
                summaryEval.setSemanticSimilarity(extractBigDecimal(quality, "sem_similarity"));
                summaryEval.setFactualAccuracy(extractBigDecimal(quality, "fact_accuracy"));
                summaryEval.setConciseness(extractBigDecimal(quality, "conciseness"));
                summaryEval.setProfessionalTone(extractBigDecimal(quality, "prof_tone"));
            }

            // Extract performance metrics
            JsonNode perf = root.path("perf");
            if (!perf.isMissingNode()) {
                summaryEval.setTotalTokens(perf.path("total_tokens").asInt(0));
                summaryEval.setLlmCallCount(perf.path("llm_calls").asInt(0));
            } else {
                summaryEval.setTotalTokens(0);
                summaryEval.setLlmCallCount(0);
            }

            LOGGER.debug(
                    "Parsed summary evaluation for job {}: overall={}, sem_similarity={}, fact_accuracy={}, conciseness={}, prof_tone={}",
                    job.getId(),
                    summaryEval.getOverallScore(),
                    summaryEval.getSemanticSimilarity(),
                    summaryEval.getFactualAccuracy(),
                    summaryEval.getConciseness(),
                    summaryEval.getProfessionalTone());

            return summaryEval;

        } catch (Exception e) {
            LOGGER.error("Failed to parse summary evaluation JSON for job {}: {}", job.getId(), e.getMessage(), e);
            LOGGER.debug("Problematic JSON: {}", summaryResultsJson);
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
}
