package com.redhat.sast.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobNodeFilterEval;
import com.redhat.sast.api.repository.MlOpsJobNodeFilterEvalRepository;

import io.fabric8.tekton.v1.PipelineRun;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
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
            Optional<MlOpsJobNodeFilterEval> filterEvalOpt = parseFilterEval(filterResultsJson, job);
            filterEvalOpt.ifPresentOrElse(
                    filterEval -> {
                        try {
                            filterEvalRepository.persist(filterEval);
                            LOGGER.info(
                                    "Successfully saved filter evaluation for MLOps job {}: tokens={}, llm_calls={}",
                                    jobId,
                                    filterEval.getTotalTokens(),
                                    filterEval.getLlmCallCount());
                        } catch (PersistenceException e) {
                            LOGGER.error(
                                    "Failed to persist filter evaluation for MLOps job {}: {}",
                                    jobId,
                                    e.getMessage(),
                                    e);
                        }
                    },
                    () -> LOGGER.warn(
                            "Could not parse filter-evaluation-results for job {} - invalid JSON format", jobId));

        } catch (Exception e) {
            LOGGER.error("Failed to extract and save filter evaluation for MLOps job {}: {}", jobId, e.getMessage(), e);
            // Don't rethrow - filter evaluation failure shouldn't break job completion
        }
    }

    /**
     * Parses filter evaluation JSON and creates MlOpsJobNodeFilterEval entity.
     * Format: {"faiss_stratified_stats": {...}}
     *
     * @return Optional containing the parsed entity, or empty if parsing fails or validation fails
     */
    private Optional<MlOpsJobNodeFilterEval> parseFilterEval(String filterResultsJson, MlOpsJob job) {
        try {
            JsonNode root = objectMapper.readTree(filterResultsJson);

            MlOpsJobNodeFilterEval filterEval = new MlOpsJobNodeFilterEval();
            filterEval.setMlOpsJob(job);

            // Extract FAISS stratified stats into individual columns
            JsonNode faissStats = root.path("faiss_stratified_stats");
            if (!faissStats.isMissingNode() && !faissStats.isNull()) {
                JsonNode withExpected = faissStats.path("with_expected_matches");
                if (!withExpected.isMissingNode()) {
                    int withExpectedTotal = withExpected.path("total").asInt(0);
                    int withExpectedFaissFound =
                            withExpected.path("faiss_found_matches").asInt(0);

                    // Validate non-negative totals
                    if (withExpectedTotal < 0 || withExpectedFaissFound < 0) {
                        LOGGER.warn(
                                "Invalid negative values in with_expected_matches for job {}: total={}, faiss_found={}",
                                job.getId(),
                                withExpectedTotal,
                                withExpectedFaissFound);
                        return Optional.empty();
                    }

                    filterEval.setWithExpectedTotal(withExpectedTotal);
                    filterEval.setWithExpectedFaissFound(withExpectedFaissFound);
                    filterEval.setWithExpectedPercCorrect(extractBigDecimal(withExpected, "perc_correct"));
                }

                JsonNode withoutExpected = faissStats.path("without_expected_matches");
                if (!withoutExpected.isMissingNode()) {
                    int withoutExpectedTotal = withoutExpected.path("total").asInt(0);
                    int withoutExpectedFaissFound =
                            withoutExpected.path("faiss_found_matches").asInt(0);

                    // Validate non-negative totals
                    if (withoutExpectedTotal < 0 || withoutExpectedFaissFound < 0) {
                        LOGGER.warn(
                                "Invalid negative values in without_expected_matches for job {}: total={}, faiss_found={}",
                                job.getId(),
                                withoutExpectedTotal,
                                withoutExpectedFaissFound);
                        return Optional.empty();
                    }

                    filterEval.setWithoutExpectedTotal(withoutExpectedTotal);
                    filterEval.setWithoutExpectedFaissFound(withoutExpectedFaissFound);
                    filterEval.setWithoutExpectedPercCorrect(extractBigDecimal(withoutExpected, "perc_correct"));
                }
            }

            // Extract performance metrics if available
            JsonNode perf = root.path("perf");
            if (!perf.isMissingNode()) {
                int totalTokens = perf.path("total_tokens").asInt(0);
                int llmCallCount = perf.path("llm_calls").asInt(0);

                // Validate non-negative values
                if (totalTokens < 0 || llmCallCount < 0) {
                    LOGGER.warn(
                            "Invalid negative performance metrics for job {}: tokens={}, llm_calls={}",
                            job.getId(),
                            totalTokens,
                            llmCallCount);
                    return Optional.empty();
                }

                filterEval.setTotalTokens(totalTokens);
                filterEval.setLlmCallCount(llmCallCount);
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

            return Optional.of(filterEval);

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse filter evaluation JSON for job {}: {}", job.getId(), e.getMessage());
            LOGGER.debug("Problematic JSON: {}", filterResultsJson);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Unexpected error parsing filter evaluation for job {}: {}", job.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Helper to extract BigDecimal from JsonNode with precise scale matching DECIMAL(5,4).
     * Validates that percentage values are within [0, 1] range.
     *
     * @param node the JSON node containing the field
     * @param fieldName the field name to extract
     * @return BigDecimal with scale 4, or ZERO if missing/invalid
     */
    private BigDecimal extractBigDecimal(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || !node.has(fieldName)) {
            return BigDecimal.ZERO;
        }

        try {
            // Get value as text to avoid double precision issues
            String textValue = node.path(fieldName).asText();
            if (textValue == null || textValue.isBlank()) {
                return BigDecimal.ZERO;
            }

            // Create BigDecimal from string for exact precision
            BigDecimal value = new BigDecimal(textValue).setScale(4, RoundingMode.HALF_UP);

            // Validate percentage is within [0, 1] range
            if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
                LOGGER.warn("Percentage value {} for field {} is outside [0, 1] range, clamping", value, fieldName);
                // Clamp to valid range
                if (value.compareTo(BigDecimal.ZERO) < 0) {
                    return BigDecimal.ZERO;
                }
                if (value.compareTo(BigDecimal.ONE) > 0) {
                    return BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP);
                }
            }

            return value;

        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid number format for field {}: {}", fieldName, e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
