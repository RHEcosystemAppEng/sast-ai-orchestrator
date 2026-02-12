package com.redhat.sast.api.platform;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobSettings;

import io.fabric8.tekton.v1.Param;

/**
 * Unit tests for PipelineParameterMapper.
 * Tests cover:
 * - OSH task ID injection for OSH_SCAN jobs
 * - Input source type parameter handling
 * - Null value handling for optional fields
 */
@DisplayName("Pipeline Parameter Mapper Tests")
class PipelineParameterMapperTest {

    private PipelineParameterMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PipelineParameterMapper();
        // Set required config properties using public setters
        mapper.setGcsBucketName(Optional.of("test-bucket"));
        mapper.setS3EndpointUrl(Optional.of("http://test-s3-endpoint"));
        mapper.setS3BucketName(Optional.of("test-s3-bucket"));
        mapper.setProfile("test");
    }

    @Test
    @DisplayName("Should include OSH_TASK_ID parameter for OSH_SCAN jobs")
    void extractPipelineParams_includesOshTaskIdForOshScan() {
        Job job = createOshScanJob("12345");

        List<Param> params = mapper.extractPipelineParams(job);

        Optional<Param> oshTaskIdParam = findParam(params, "OSH_TASK_ID");
        assertTrue(oshTaskIdParam.isPresent(), "OSH_TASK_ID parameter should be present for OSH_SCAN jobs");
        assertEquals("12345", oshTaskIdParam.get().getValue().getStringVal());
    }

    @Test
    @DisplayName("Should handle null OSH scan ID gracefully")
    void extractPipelineParams_handlesNullOshScanId() {
        Job job = createOshScanJob(null);

        List<Param> params = mapper.extractPipelineParams(job);

        Optional<Param> oshTaskIdParam = findParam(params, "OSH_TASK_ID");
        assertTrue(oshTaskIdParam.isPresent(), "OSH_TASK_ID parameter should be present even when null");
        assertEquals(
                "", oshTaskIdParam.get().getValue().getStringVal(), "Null OSH scan ID should result in empty string");
    }

    @Test
    @DisplayName("Should NOT include OSH_TASK_ID parameter for GOOGLE_SHEET jobs")
    void extractPipelineParams_excludesOshTaskIdForGoogleSheet() {
        Job job = createGoogleSheetJob();

        List<Param> params = mapper.extractPipelineParams(job);

        Optional<Param> oshTaskIdParam = findParam(params, "OSH_TASK_ID");
        assertFalse(oshTaskIdParam.isPresent(), "OSH_TASK_ID parameter should NOT be present for GOOGLE_SHEET jobs");
    }

    @Test
    @DisplayName("Should set INPUT_SOURCE_TYPE to OSH_SCAN for OSH jobs")
    void extractPipelineParams_setsCorrectInputSourceTypeForOsh() {
        Job job = createOshScanJob("12345");

        List<Param> params = mapper.extractPipelineParams(job);

        Optional<Param> inputSourceTypeParam = findParam(params, "INPUT_SOURCE_TYPE");
        assertTrue(inputSourceTypeParam.isPresent());
        assertEquals("OSH_SCAN", inputSourceTypeParam.get().getValue().getStringVal());
    }

    @Test
    @DisplayName("Should set INPUT_SOURCE_TYPE to GOOGLE_SHEET for Google Sheet jobs")
    void extractPipelineParams_setsCorrectInputSourceTypeForGoogleSheet() {
        Job job = createGoogleSheetJob();

        List<Param> params = mapper.extractPipelineParams(job);

        Optional<Param> inputSourceTypeParam = findParam(params, "INPUT_SOURCE_TYPE");
        assertTrue(inputSourceTypeParam.isPresent());
        assertEquals("GOOGLE_SHEET", inputSourceTypeParam.get().getValue().getStringVal());
    }

    // ========== MLOps LLM Override Tests ==========

    @Test
    @DisplayName("Should override LLM URL, model name, and API type from MlOpsJobSettings when provided")
    void extractMlOpsPipelineParams_overridesLlmUrlModelAndTypeFromSettings() {
        MlOpsJob mlOpsJob = createMlOpsJobWithOverrides("https://custom-llm.example.com", "gpt-4", "openai");

        List<Param> params = mapper.extractMlOpsPipelineParams(mlOpsJob, "v1.0.0", "v1.0.0", "quay.io/test:latest");

        // Verify LLM URL override
        Optional<Param> llmUrlParam = findParam(params, "LLM_URL");
        assertTrue(llmUrlParam.isPresent(), "LLM_URL parameter should be present");
        assertEquals(
                "https://custom-llm.example.com",
                llmUrlParam.get().getValue().getStringVal(),
                "LLM_URL should use value from MlOpsJobSettings, not secret");

        // Verify LLM API key comes from secret only (not from settings)
        Optional<Param> llmApiKeyParam = findParam(params, "LLM_API_KEY");
        assertTrue(llmApiKeyParam.isPresent(), "LLM_API_KEY parameter should be present");
        assertEquals(
                "test-api-key",
                llmApiKeyParam.get().getValue().getStringVal(),
                "LLM_API_KEY should always use value from secret for security");

        // Verify model name override
        Optional<Param> llmModelNameParam = findParam(params, "LLM_MODEL_NAME");
        assertTrue(llmModelNameParam.isPresent(), "LLM_MODEL_NAME parameter should be present");
        assertEquals("gpt-4", llmModelNameParam.get().getValue().getStringVal());

        // Verify API type override
        Optional<Param> llmApiTypeParam = findParam(params, "LLM_API_TYPE");
        assertTrue(llmApiTypeParam.isPresent(), "LLM_API_TYPE parameter should be present");
        assertEquals("openai", llmApiTypeParam.get().getValue().getStringVal());
    }

    @Test
    @DisplayName("Should fallback to secret values when MlOpsJobSettings has no LLM overrides")
    void extractMlOpsPipelineParams_fallsBackToSecretValues() {
        MlOpsJob mlOpsJob = createMlOpsJobWithoutOverrides();

        List<Param> params = mapper.extractMlOpsPipelineParams(mlOpsJob, "v1.0.0", "v1.0.0", "quay.io/test:latest");

        // In test mode, these values come from mock secret (see PipelineParameterMapper.getLlmSecretValues)
        Optional<Param> llmUrlParam = findParam(params, "LLM_URL");
        assertTrue(llmUrlParam.isPresent());
        assertEquals(
                "http://test-llm-url",
                llmUrlParam.get().getValue().getStringVal(),
                "LLM_URL should fallback to secret value");

        Optional<Param> llmApiKeyParam = findParam(params, "LLM_API_KEY");
        assertTrue(llmApiKeyParam.isPresent());
        assertEquals(
                "test-api-key",
                llmApiKeyParam.get().getValue().getStringVal(),
                "LLM_API_KEY should fallback to secret value");
    }

    @Test
    @DisplayName("Should override embedding LLM URL and model name from MlOpsJobSettings when provided")
    void extractMlOpsPipelineParams_overridesEmbeddingLlmUrlAndModelName() {
        MlOpsJob mlOpsJob =
                createMlOpsJobWithEmbeddingOverrides("https://custom-embeddings.example.com", "text-embedding-3-large");

        List<Param> params = mapper.extractMlOpsPipelineParams(mlOpsJob, "v1.0.0", "v1.0.0", "quay.io/test:latest");

        // Verify embedding LLM URL override
        Optional<Param> embeddingUrlParam = findParam(params, "EMBEDDINGS_LLM_URL");
        assertTrue(embeddingUrlParam.isPresent(), "EMBEDDINGS_LLM_URL parameter should be present");
        assertEquals(
                "https://custom-embeddings.example.com",
                embeddingUrlParam.get().getValue().getStringVal(),
                "EMBEDDINGS_LLM_URL should use value from MlOpsJobSettings");

        // Verify embedding LLM API key comes from secret only (not from settings)
        Optional<Param> embeddingApiKeyParam = findParam(params, "EMBEDDINGS_LLM_API_KEY");
        assertTrue(embeddingApiKeyParam.isPresent(), "EMBEDDINGS_LLM_API_KEY parameter should be present");
        assertEquals(
                "test-embeddings-key",
                embeddingApiKeyParam.get().getValue().getStringVal(),
                "EMBEDDINGS_LLM_API_KEY should always use value from secret for security");

        // Verify embedding model name override
        Optional<Param> embeddingModelParam = findParam(params, "EMBEDDINGS_LLM_MODEL_NAME");
        assertTrue(embeddingModelParam.isPresent(), "EMBEDDINGS_LLM_MODEL_NAME parameter should be present");
        assertEquals(
                "text-embedding-3-large", embeddingModelParam.get().getValue().getStringVal());
    }

    @Test
    @DisplayName("Should handle mixed overrides - some from settings, some from secret")
    void extractMlOpsPipelineParams_handlesMixedOverrides() {
        // Override URL from settings, API key always from secret
        MlOpsJob mlOpsJob = createMlOpsJobWithPartialOverrides();

        List<Param> params = mapper.extractMlOpsPipelineParams(mlOpsJob, "v1.0.0", "v1.0.0", "quay.io/test:latest");

        // URL should be overridden from settings
        Optional<Param> llmUrlParam = findParam(params, "LLM_URL");
        assertTrue(llmUrlParam.isPresent());
        assertEquals(
                "https://partial-override.example.com",
                llmUrlParam.get().getValue().getStringVal());

        // API key always comes from secret (not overridable for security)
        Optional<Param> llmApiKeyParam = findParam(params, "LLM_API_KEY");
        assertTrue(llmApiKeyParam.isPresent());
        assertEquals(
                "test-api-key",
                llmApiKeyParam.get().getValue().getStringVal(),
                "LLM_API_KEY should always come from secret for security");
    }

    // Helper methods
    private Job createOshScanJob(String oshScanId) {
        Job job = new Job();
        job.setId(1L);
        job.setInputSourceType(InputSourceType.OSH_SCAN);
        job.setOshScanId(oshScanId);
        job.setProjectName("test-project");
        job.setProjectVersion("1.0.0");
        job.setGSheetUrl("https://osh.example.com/report");
        job.setPackageSourceCodeUrl("https://github.com/test/repo");
        job.setKnownFalsePositivesUrl("");
        return job;
    }

    private Job createGoogleSheetJob() {
        Job job = new Job();
        job.setId(2L);
        job.setInputSourceType(InputSourceType.GOOGLE_SHEET);
        job.setProjectName("test-project");
        job.setProjectVersion("1.0.0");
        job.setGSheetUrl("https://docs.google.com/spreadsheets/d/test");
        job.setPackageSourceCodeUrl("https://github.com/test/repo");
        job.setKnownFalsePositivesUrl("");
        return job;
    }

    private Optional<Param> findParam(List<Param> params, String name) {
        return params.stream().filter(p -> name.equals(p.getName())).findFirst();
    }

    // MLOps Job helper methods
    private MlOpsJob createMlOpsJobWithOverrides(String llmUrl, String llmModelName, String llmApiType) {
        MlOpsJob mlOpsJob = new MlOpsJob();
        mlOpsJob.setId(100L);
        mlOpsJob.setPackageNvr("test-package-1.0.0-1.el9");
        mlOpsJob.setPackageSourceCodeUrl("https://github.com/test/repo");
        mlOpsJob.setProjectName("test-package");
        mlOpsJob.setProjectVersion("1.0.0-1.el9");
        mlOpsJob.setKnownFalsePositivesUrl("");

        MlOpsJobSettings settings = new MlOpsJobSettings();
        settings.setLlmUrl(llmUrl);
        // API key always comes from secret, never from settings
        settings.setLlmModelName(llmModelName);
        settings.setLlmApiType(llmApiType);
        settings.setSecretName("test-secret");

        mlOpsJob.setMlOpsJobSettings(settings);
        return mlOpsJob;
    }

    private MlOpsJob createMlOpsJobWithoutOverrides() {
        MlOpsJob mlOpsJob = new MlOpsJob();
        mlOpsJob.setId(101L);
        mlOpsJob.setPackageNvr("test-package-1.0.0-1.el9");
        mlOpsJob.setPackageSourceCodeUrl("https://github.com/test/repo");
        mlOpsJob.setProjectName("test-package");
        mlOpsJob.setProjectVersion("1.0.0-1.el9");
        mlOpsJob.setKnownFalsePositivesUrl("");

        MlOpsJobSettings settings = new MlOpsJobSettings();
        settings.setSecretName("test-secret");
        // No overrides - all null

        mlOpsJob.setMlOpsJobSettings(settings);
        return mlOpsJob;
    }

    private MlOpsJob createMlOpsJobWithEmbeddingOverrides(String embeddingUrl, String embeddingModelName) {
        MlOpsJob mlOpsJob = new MlOpsJob();
        mlOpsJob.setId(102L);
        mlOpsJob.setPackageNvr("test-package-1.0.0-1.el9");
        mlOpsJob.setPackageSourceCodeUrl("https://github.com/test/repo");
        mlOpsJob.setProjectName("test-package");
        mlOpsJob.setProjectVersion("1.0.0-1.el9");
        mlOpsJob.setKnownFalsePositivesUrl("");

        MlOpsJobSettings settings = new MlOpsJobSettings();
        settings.setEmbeddingLlmUrl(embeddingUrl);
        // Embedding API key always comes from secret, never from settings
        settings.setEmbeddingLlmModelName(embeddingModelName);
        settings.setSecretName("test-secret");

        mlOpsJob.setMlOpsJobSettings(settings);
        return mlOpsJob;
    }

    private MlOpsJob createMlOpsJobWithPartialOverrides() {
        MlOpsJob mlOpsJob = new MlOpsJob();
        mlOpsJob.setId(103L);
        mlOpsJob.setPackageNvr("test-package-1.0.0-1.el9");
        mlOpsJob.setPackageSourceCodeUrl("https://github.com/test/repo");
        mlOpsJob.setProjectName("test-package");
        mlOpsJob.setProjectVersion("1.0.0-1.el9");
        mlOpsJob.setKnownFalsePositivesUrl("");

        MlOpsJobSettings settings = new MlOpsJobSettings();
        settings.setLlmUrl("https://partial-override.example.com");
        // llmApiKey is null - should fall back to secret
        settings.setSecretName("test-secret");

        mlOpsJob.setMlOpsJobSettings(settings);
        return mlOpsJob;
    }
}
