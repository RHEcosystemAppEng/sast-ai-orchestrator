package com.redhat.sast.api.service.mlops;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobNodeFilterEval;
import com.redhat.sast.api.repository.mlops.MlOpsJobNodeFilterEvalRepository;

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

            // Parse stratified match statistics
            if (!isValidStratifiedStats(root, filterEval, job)) {
                return Optional.empty();
            }

            // Parse LLM token metrics
            if (!isValidTokenMetrics(root, filterEval, job)) {
                return Optional.empty();
            }

            logParsedResults(filterEval, job);
            return Optional.of(filterEval);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse filter evaluation JSON for job {}: {}", job.getId(), e.getMessage());
            LOGGER.debug("Problematic JSON: {}", filterResultsJson);
        } catch (Exception e) {
            LOGGER.error("Unexpected error parsing filter evaluation for job {}: {}", job.getId(), e.getMessage(), e);
        }
        return Optional.empty();
    }

    private boolean isValidStratifiedStats(JsonNode root, MlOpsJobNodeFilterEval filterEval, MlOpsJob job) {
        JsonNode faissStats = root.path("faiss_stratified_stats");
        if (faissStats.isMissingNode() || faissStats.isNull()) {
            return true; // Missing stratified stats is OK
        }

        return isValidExpectedSection(faissStats, filterEval, job)
                && isValidUnexpectedSection(faissStats, filterEval, job);
    }

    private boolean isValidExpectedSection(JsonNode faissStats, MlOpsJobNodeFilterEval filterEval, MlOpsJob job) {
        JsonNode withExpected = faissStats.path("with_expected_matches");
        if (withExpected.isMissingNode()) {
            return true; // Missing section is OK
        }

        int total = withExpected.path("total").asInt(0);
        int faissFound = withExpected.path("faiss_found_matches").asInt(0);

        if (validateNonNegativeCounts(total, faissFound, "with_expected_matches", job)) {
            return false;
        }

        filterEval.setWithExpectedTotal(total);
        filterEval.setWithExpectedFaissFound(faissFound);
        filterEval.setWithExpectedPercCorrect(extractBigDecimal(withExpected, "perc_correct"));
        return true;
    }

    private boolean isValidUnexpectedSection(JsonNode faissStats, MlOpsJobNodeFilterEval filterEval, MlOpsJob job) {
        JsonNode withoutExpected = faissStats.path("without_expected_matches");
        if (withoutExpected.isMissingNode()) {
            return true; // Missing section is OK
        }

        int total = withoutExpected.path("total").asInt(0);
        int faissFound = withoutExpected.path("faiss_found_matches").asInt(0);

        if (validateNonNegativeCounts(total, faissFound, "without_expected_matches", job)) {
            return false;
        }

        filterEval.setWithoutExpectedTotal(total);
        filterEval.setWithoutExpectedFaissFound(faissFound);
        filterEval.setWithoutExpectedPercCorrect(extractBigDecimal(withoutExpected, "perc_correct"));
        return true;
    }

    private boolean isValidTokenMetrics(JsonNode root, MlOpsJobNodeFilterEval filterEval, MlOpsJob job) {
        JsonNode perf = root.path("perf");
        if (perf.isMissingNode()) {
            // Set defaults when token metrics are missing
            filterEval.setTotalTokens(0);
            filterEval.setLlmCallCount(0);
            return true;
        }

        int totalTokens = perf.path("total_tokens").asInt(0);
        int llmCallCount = perf.path("llm_calls").asInt(0);

        if (validateNonNegativeCounts(totalTokens, llmCallCount, "token metrics", job)) {
            return false;
        }

        filterEval.setTotalTokens(totalTokens);
        filterEval.setLlmCallCount(llmCallCount);
        return true;
    }

    /**
     * Validates that count values are non-negative.
     */
    private boolean validateNonNegativeCounts(int count1, int count2, String section, MlOpsJob job) {
        if (count1 < 0 || count2 < 0) {
            LOGGER.warn(
                    "Invalid negative values in {} for job {}: first={}, second={}",
                    section,
                    job.getId(),
                    count1,
                    count2);
            return true;
        }
        return false;
    }

    private void logParsedResults(MlOpsJobNodeFilterEval filterEval, MlOpsJob job) {
        LOGGER.debug(
                "Parsed filter evaluation for job {}: with_expected_total={}, without_expected_total={}, tokens={}, llm_calls={}",
                job.getId(),
                filterEval.getWithExpectedTotal(),
                filterEval.getWithoutExpectedTotal(),
                filterEval.getTotalTokens(),
                filterEval.getLlmCallCount());
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
