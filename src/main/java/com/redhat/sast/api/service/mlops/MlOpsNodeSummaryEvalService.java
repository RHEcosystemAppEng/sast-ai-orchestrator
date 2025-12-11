package com.redhat.sast.api.service.mlops;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobNodeSummaryEval;
import com.redhat.sast.api.repository.mlops.MlOpsJobNodeSummaryEvalRepository;
import com.redhat.sast.api.util.JsonParsingUtils;

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

    private static final String SUMMARY_EVAL_RESULTS_KEY = "summary-evaluation-results";

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
            String summaryResultsJson = mlOpsMetricsService.extractTektonResult(pipelineRun, SUMMARY_EVAL_RESULTS_KEY);
            if (summaryResultsJson == null || summaryResultsJson.isBlank() || "{}".equals(summaryResultsJson.trim())) {
                LOGGER.debug("No {} found for job {} - summary evaluation not run", SUMMARY_EVAL_RESULTS_KEY, jobId);
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

            // Extract quality metrics (using null-safe extractor for NOT NULL columns)
            JsonNode quality = root.path("quality");
            if (!quality.isMissingNode()) {
                summaryEval.setOverallScore(JsonParsingUtils.extractPercentageOrZero(quality, "overall"));
                summaryEval.setSemanticSimilarity(JsonParsingUtils.extractPercentageOrZero(quality, "sem_similarity"));
                summaryEval.setFactualAccuracy(JsonParsingUtils.extractPercentageOrZero(quality, "fact_accuracy"));
                summaryEval.setConciseness(JsonParsingUtils.extractPercentageOrZero(quality, "conciseness"));
                summaryEval.setProfessionalTone(JsonParsingUtils.extractPercentageOrZero(quality, "prof_tone"));
            }

            // Extract performance metrics (asInt handles missing nodes with default 0)
            JsonNode perf = root.path("perf");
            summaryEval.setTotalTokens(perf.path("total_tokens").asInt(0));
            summaryEval.setLlmCallCount(perf.path("llm_calls").asInt(0));

            LOGGER.debug(
                    "Parsed summary evaluation for job {}: overall={}, sem_similarity={}, fact_accuracy={}, conciseness={}, prof_tone={}",
                    job.getId(),
                    summaryEval.getOverallScore(),
                    summaryEval.getSemanticSimilarity(),
                    summaryEval.getFactualAccuracy(),
                    summaryEval.getConciseness(),
                    summaryEval.getProfessionalTone());

            return summaryEval;

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse summary evaluation JSON for job {}: {}", job.getId(), e.getMessage(), e);
            LOGGER.debug("Problematic JSON: {}", summaryResultsJson);
            return null;
        }
    }
}
