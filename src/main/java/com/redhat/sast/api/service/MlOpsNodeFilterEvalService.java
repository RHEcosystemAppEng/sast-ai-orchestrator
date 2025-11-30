package com.redhat.sast.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobNodeFilterEval;
import com.redhat.sast.api.repository.MlOpsJobNodeFilterEvalRepository;

import io.fabric8.tekton.v1.PipelineRun;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for extracting and storing filter node evaluation results from Tekton pipeline runs.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MlOpsNodeFilterEvalService {

    private final MlOpsJobNodeFilterEvalRepository filterEvalRepository;
    private final MlOpsJobService mlOpsJobService;
    private final MlOpsMetricsService mlOpsMetricsService;
    private final ObjectMapper objectMapper;

    /**
     * Extracts filter node evaluation results from PipelineRun and saves to database.
     * Gracefully handles missing results (when filter evaluation is not run).
     */
    @Transactional
    public void extractAndSaveFilterEval(Long jobId, PipelineRun pipelineRun) {
        try {
            MlOpsJob job = mlOpsJobService.getJobEntityById(jobId);
            if (job == null) {
                LOGGER.warn("MLOps job {} not found, cannot save filter evaluation", jobId);
                return;
            }

            // Extract filter-evaluation-results from Tekton
            String filterResultsJson =
                    mlOpsMetricsService.extractTektonResult(pipelineRun, "filter-evaluation-results");
            if (filterResultsJson == null || filterResultsJson.isBlank() || "{}".equals(filterResultsJson.trim())) {
                LOGGER.debug("No filter-evaluation-results found for job {} - filter evaluation not run", jobId);
                return; // Gracefully skip - filter evaluation is optional
            }

            // Parse and save filter evaluation
            MlOpsJobNodeFilterEval filterEval = parseFilterEval(filterResultsJson, job);
            if (filterEval != null) {
                filterEvalRepository.persist(filterEval);
                LOGGER.info(
                        "Successfully saved filter evaluation for MLOps job {}: tokens={}, llm_calls={}",
                        jobId,
                        filterEval.getTotalTokens(),
                        filterEval.getLlmCallCount());
            } else {
                LOGGER.warn("Could not parse filter-evaluation-results for job {} - invalid JSON format", jobId);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to extract and save filter evaluation for MLOps job {}: {}", jobId, e.getMessage(), e);
            // Don't rethrow - filter evaluation failure shouldn't break job completion
        }
    }

    /**
     * Parses filter evaluation JSON and creates MlOpsJobNodeFilterEval entity.
     * Format: {"faiss_stratified_stats": {...}}
     */
    private MlOpsJobNodeFilterEval parseFilterEval(String filterResultsJson, MlOpsJob job) {
        try {
            JsonNode root = objectMapper.readTree(filterResultsJson);

            MlOpsJobNodeFilterEval filterEval = new MlOpsJobNodeFilterEval();
            filterEval.setMlOpsJob(job);

            // Extract FAISS stratified stats into individual columns
            JsonNode faissStats = root.path("faiss_stratified_stats");
            if (!faissStats.isMissingNode() && !faissStats.isNull()) {
                JsonNode withExpected = faissStats.path("with_expected_matches");
                if (!withExpected.isMissingNode()) {
                    filterEval.setWithExpectedTotal(withExpected.path("total").asInt(0));
                    filterEval.setWithExpectedFaissFound(
                            withExpected.path("faiss_found_matches").asInt(0));
                    filterEval.setWithExpectedPercCorrect(extractBigDecimal(withExpected, "perc_correct"));
                }

                JsonNode withoutExpected = faissStats.path("without_expected_matches");
                if (!withoutExpected.isMissingNode()) {
                    filterEval.setWithoutExpectedTotal(
                            withoutExpected.path("total").asInt(0));
                    filterEval.setWithoutExpectedFaissFound(
                            withoutExpected.path("faiss_found_matches").asInt(0));
                    filterEval.setWithoutExpectedPercCorrect(extractBigDecimal(withoutExpected, "perc_correct"));
                }
            }

            // Extract performance metrics if available
            JsonNode perf = root.path("perf");
            if (!perf.isMissingNode()) {
                filterEval.setTotalTokens(perf.path("total_tokens").asInt(0));
                filterEval.setLlmCallCount(perf.path("llm_calls").asInt(0));
            } else {
                filterEval.setTotalTokens(0);
                filterEval.setLlmCallCount(0);
            }

            LOGGER.debug(
                    "Parsed filter evaluation for job {}: with_expected_total={}, without_expected_total={}, tokens={}, llm_calls={}",
                    job.getId(),
                    filterEval.getWithExpectedTotal(),
                    filterEval.getWithoutExpectedTotal(),
                    filterEval.getTotalTokens(),
                    filterEval.getLlmCallCount());

            return filterEval;

        } catch (Exception e) {
            LOGGER.error("Failed to parse filter evaluation JSON for job {}: {}", job.getId(), e.getMessage(), e);
            LOGGER.debug("Problematic JSON: {}", filterResultsJson);
            return null;
        }
    }

    /**
     * Helper to extract BigDecimal from JsonNode
     */
    private java.math.BigDecimal extractBigDecimal(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || !node.has(fieldName)) {
            return java.math.BigDecimal.ZERO;
        }
        double value = node.path(fieldName).asDouble(0.0);
        return java.math.BigDecimal.valueOf(value);
    }
}
