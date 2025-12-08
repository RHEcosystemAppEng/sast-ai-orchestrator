package com.redhat.sast.api.platform;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.model.Job;

import io.fabric8.tekton.v1.Param;

/**
 * Unit tests for PipelineParameterMapper.
 *
 * Tests cover:
 * - OSH task ID injection for OSH_SCAN jobs
 * - Input source type parameter handling
 * - Null value handling for optional fields
 */
@DisplayName("Pipeline Parameter Mapper Tests")
class PipelineParameterMapperTest {

    private PipelineParameterMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        mapper = new PipelineParameterMapper();
        // Set required config properties via reflection for testing
        setField(mapper, "gcsBucketName", Optional.of("test-bucket"));
        setField(mapper, "profile", "test");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("Should include OSH_TASK_ID parameter for OSH_SCAN jobs")
    void testExtractPipelineParams_includesOshTaskIdForOshScan() {
        Job job = createOshScanJob("12345");

        List<Param> params = mapper.extractPipelineParams(job);

        Optional<Param> oshTaskIdParam = findParam(params, "OSH_TASK_ID");
        assertTrue(oshTaskIdParam.isPresent(), "OSH_TASK_ID parameter should be present for OSH_SCAN jobs");
        assertEquals("12345", oshTaskIdParam.get().getValue().getStringVal());
    }

    @Test
    @DisplayName("Should handle null OSH scan ID gracefully")
    void testExtractPipelineParams_handlesNullOshScanId() {
        Job job = createOshScanJob(null);

        List<Param> params = mapper.extractPipelineParams(job);

        Optional<Param> oshTaskIdParam = findParam(params, "OSH_TASK_ID");
        assertTrue(oshTaskIdParam.isPresent(), "OSH_TASK_ID parameter should be present even when null");
        assertEquals(
                "", oshTaskIdParam.get().getValue().getStringVal(), "Null OSH scan ID should result in empty string");
    }

    @Test
    @DisplayName("Should NOT include OSH_TASK_ID parameter for GOOGLE_SHEET jobs")
    void testExtractPipelineParams_excludesOshTaskIdForGoogleSheet() {
        Job job = createGoogleSheetJob();

        List<Param> params = mapper.extractPipelineParams(job);

        Optional<Param> oshTaskIdParam = findParam(params, "OSH_TASK_ID");
        assertFalse(oshTaskIdParam.isPresent(), "OSH_TASK_ID parameter should NOT be present for GOOGLE_SHEET jobs");
    }

    @Test
    @DisplayName("Should set INPUT_SOURCE_TYPE to OSH_SCAN for OSH jobs")
    void testExtractPipelineParams_setsCorrectInputSourceTypeForOsh() {
        Job job = createOshScanJob("12345");

        List<Param> params = mapper.extractPipelineParams(job);

        Optional<Param> inputSourceTypeParam = findParam(params, "INPUT_SOURCE_TYPE");
        assertTrue(inputSourceTypeParam.isPresent());
        assertEquals("OSH_SCAN", inputSourceTypeParam.get().getValue().getStringVal());
    }

    @Test
    @DisplayName("Should set INPUT_SOURCE_TYPE to GOOGLE_SHEET for Google Sheet jobs")
    void testExtractPipelineParams_setsCorrectInputSourceTypeForGoogleSheet() {
        Job job = createGoogleSheetJob();

        List<Param> params = mapper.extractPipelineParams(job);

        Optional<Param> inputSourceTypeParam = findParam(params, "INPUT_SOURCE_TYPE");
        assertTrue(inputSourceTypeParam.isPresent());
        assertEquals("GOOGLE_SHEET", inputSourceTypeParam.get().getValue().getStringVal());
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
}
