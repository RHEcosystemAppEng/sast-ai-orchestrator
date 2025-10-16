package com.redhat.sast.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.v1.dto.osh.OshScanResponse;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;

/**
 * Integration tests for OshJobCreationService.
 *
 * Tests cover:
 * - Job creation from OSH scans
 * - Idempotency handling
 * - NVR building from OSH metadata
 * - Error handling and edge cases
 * - JSON content integration
 */
@QuarkusTest
@DisplayName("OSH Job Creation Service Tests")
class OshJobCreationServiceTest {

    @Inject
    OshJobCreationService oshJobCreationService;

    @InjectMock
    JobService jobService;

    @InjectMock
    OshJsonDownloadService oshJsonDownloadService;

    @InjectMock
    JobRepository jobRepository;

    private OshScanResponse createValidOshScan() {
        OshScanResponse scan = new OshScanResponse();
        scan.setScanId(12345);
        scan.setState("CLOSED");
        scan.setComponent("systemd");
        scan.setVersion("252");

        // Add raw data with Label field
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("Label", "systemd-252-54.el9.src.rpm");
        scan.setRawData(rawData);

        return scan;
    }

    private Job createTestJob() {
        Job job = new Job();
        job.setId(1L);
        job.setPackageNvr("systemd-252-54.el9");
        job.setOshScanId("12345");
        return job;
    }

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(jobService, oshJsonDownloadService, jobRepository);
    }

    @Test
    @DisplayName("Should create job successfully from valid OSH scan")
    void testCreateJobFromOshScan_Success() {
        // Given
        OshScanResponse scan = createValidOshScan();
        String jsonContent = "{\"vulnerabilities\": []}";
        Job expectedJob = createTestJob();

        when(jobRepository.findByOshScanId("12345")).thenReturn(Optional.empty());
        when(oshJsonDownloadService.downloadSastReport(12345)).thenReturn(Optional.of(jsonContent));
        when(jobService.createJobEntity(any(JobCreationDto.class))).thenReturn(expectedJob);

        // When
        Optional<Job> result = oshJobCreationService.createJobFromOshScan(scan);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedJob, result.get());

        // Verify interactions
        verify(jobRepository).findByOshScanId("12345");
        verify(oshJsonDownloadService).downloadSastReport(12345);
        verify(jobService)
                .createJobEntity(argThat(dto -> dto.getPackageNvr().equals("systemd-252-54.el9")
                        && dto.getJsonContent().equals(jsonContent)
                        && dto.getOshScanId().equals("12345")
                        && dto.getSubmittedBy().equals("OSH_SCHEDULER")));
    }

    @Test
    @DisplayName("Should skip job creation if already processed")
    void testCreateJobFromOshScan_AlreadyProcessed() {
        // Given
        OshScanResponse scan = createValidOshScan();
        Job existingJob = createTestJob();

        when(jobRepository.findByOshScanId("12345")).thenReturn(Optional.of(existingJob));

        // When
        Optional<Job> result = oshJobCreationService.createJobFromOshScan(scan);

        // Then
        assertTrue(result.isEmpty());

        // Verify no job creation attempted
        verify(jobRepository).findByOshScanId("12345");
        verify(oshJsonDownloadService, never()).downloadSastReport(any());
        verify(jobService, never()).createJobEntity(any());
    }

    @Test
    @DisplayName("Should skip job creation if no JSON available")
    void testCreateJobFromOshScan_NoJsonAvailable() {
        // Given
        OshScanResponse scan = createValidOshScan();

        when(jobRepository.findByOshScanId("12345")).thenReturn(Optional.empty());
        when(oshJsonDownloadService.downloadSastReport(12345)).thenReturn(Optional.empty());

        // When
        Optional<Job> result = oshJobCreationService.createJobFromOshScan(scan);

        // Then
        assertTrue(result.isEmpty());

        // Verify no job creation attempted
        verify(jobRepository).findByOshScanId("12345");
        verify(oshJsonDownloadService).downloadSastReport(12345);
        verify(jobService, never()).createJobEntity(any());
    }

    @Test
    @DisplayName("Should build NVR from OSH Label field")
    void testNvrBuildingFromLabel() {
        // Given
        OshScanResponse scan = createValidOshScan();
        String jsonContent = "{\"vulnerabilities\": []}";
        Job expectedJob = createTestJob();

        when(jobRepository.findByOshScanId("12345")).thenReturn(Optional.empty());
        when(oshJsonDownloadService.downloadSastReport(12345)).thenReturn(Optional.of(jsonContent));
        when(jobService.createJobEntity(any(JobCreationDto.class))).thenReturn(expectedJob);

        // When
        Optional<Job> result = oshJobCreationService.createJobFromOshScan(scan);

        // Then
        assertTrue(result.isPresent());

        // Verify NVR extracted from Label field
        verify(jobService).createJobEntity(argThat(dto -> dto.getPackageNvr().equals("systemd-252-54.el9")));
    }

    @Test
    @DisplayName("Should fallback to component-version if no Label")
    void testNvrBuildingFallback() {
        // Given
        OshScanResponse scan = createValidOshScan();
        scan.setRawData(null); // No Label field available
        String jsonContent = "{\"vulnerabilities\": []}";
        Job expectedJob = createTestJob();

        when(jobRepository.findByOshScanId("12345")).thenReturn(Optional.empty());
        when(oshJsonDownloadService.downloadSastReport(12345)).thenReturn(Optional.of(jsonContent));
        when(jobService.createJobEntity(any(JobCreationDto.class))).thenReturn(expectedJob);

        // When
        Optional<Job> result = oshJobCreationService.createJobFromOshScan(scan);

        // Then
        assertTrue(result.isPresent());

        // Verify fallback NVR format used
        verify(jobService).createJobEntity(argThat(dto -> dto.getPackageNvr().equals("systemd-252")));
    }

    @Test
    @DisplayName("Should handle complex package names correctly")
    void testComplexPackageNames() {
        // Given
        OshScanResponse scan = new OshScanResponse();
        scan.setScanId(54321);
        scan.setState("CLOSED");
        scan.setComponent("zlib-ng");
        scan.setVersion("2.1.6");

        Map<String, Object> rawData = new HashMap<>();
        rawData.put("Label", "zlib-ng-2.1.6-2.el10.src.rpm");
        scan.setRawData(rawData);

        String jsonContent = "{\"vulnerabilities\": []}";
        Job expectedJob = createTestJob();

        when(jobRepository.findByOshScanId("54321")).thenReturn(Optional.empty());
        when(oshJsonDownloadService.downloadSastReport(54321)).thenReturn(Optional.of(jsonContent));
        when(jobService.createJobEntity(any(JobCreationDto.class))).thenReturn(expectedJob);

        // When
        Optional<Job> result = oshJobCreationService.createJobFromOshScan(scan);

        // Then
        assertTrue(result.isPresent());

        // Verify complex package name handled correctly
        verify(jobService).createJobEntity(argThat(dto -> dto.getPackageNvr().equals("zlib-ng-2.1.6-2.el10")));
    }

    @Test
    @DisplayName("Should handle job creation errors gracefully")
    void testJobCreationError() {
        // Given
        OshScanResponse scan = createValidOshScan();
        String jsonContent = "{\"vulnerabilities\": []}";

        when(jobRepository.findByOshScanId("12345")).thenReturn(Optional.empty());
        when(oshJsonDownloadService.downloadSastReport(12345)).thenReturn(Optional.of(jsonContent));
        when(jobService.createJobEntity(any(JobCreationDto.class))).thenThrow(new RuntimeException("Database error"));

        // When
        Optional<Job> result = oshJobCreationService.createJobFromOshScan(scan);

        // Then
        assertTrue(result.isEmpty()); // Should return empty on error

        // Verify error handling doesn't crash
        verify(jobRepository).findByOshScanId("12345");
        verify(oshJsonDownloadService).downloadSastReport(12345);
        verify(jobService).createJobEntity(any(JobCreationDto.class));
    }

    @Test
    @DisplayName("Should validate scan eligibility correctly")
    void testCanProcessScan() {
        // Test valid scan
        OshScanResponse validScan = createValidOshScan();
        assertTrue(oshJobCreationService.canProcessScan(validScan));

        // Test null scan
        assertFalse(oshJobCreationService.canProcessScan(null));

        // Test scan with null ID
        OshScanResponse nullIdScan = createValidOshScan();
        nullIdScan.setScanId(null);
        assertFalse(oshJobCreationService.canProcessScan(nullIdScan));

        // Test scan with wrong state
        OshScanResponse openScan = createValidOshScan();
        openScan.setState("OPEN");
        assertFalse(oshJobCreationService.canProcessScan(openScan));

        // Test scan with no component
        OshScanResponse noComponentScan = createValidOshScan();
        noComponentScan.setComponent(null);
        assertFalse(oshJobCreationService.canProcessScan(noComponentScan));

        // Test scan with empty component
        OshScanResponse emptyComponentScan = createValidOshScan();
        emptyComponentScan.setComponent("");
        assertFalse(oshJobCreationService.canProcessScan(emptyComponentScan));
    }

    @Test
    @DisplayName("Should handle missing component gracefully")
    void testMissingComponent() {
        // Given
        OshScanResponse scan = createValidOshScan();
        scan.setComponent(null);

        // When/Then - should not process invalid scan
        assertFalse(oshJobCreationService.canProcessScan(scan));

        // Verify no processing attempted for invalid scan
        Optional<Job> result = oshJobCreationService.createJobFromOshScan(scan);
        assertTrue(result.isEmpty());

        verify(jobRepository, never()).findByOshScanId(any());
        verify(oshJsonDownloadService, never()).downloadSastReport(any());
        verify(jobService, never()).createJobEntity(any());
    }

    @Test
    @DisplayName("Should handle idempotency check errors gracefully")
    void testIdempotencyCheckError() {
        // Given
        OshScanResponse scan = createValidOshScan();

        when(jobRepository.findByOshScanId("12345")).thenThrow(new RuntimeException("Database connection error"));

        // When
        Optional<Job> result = oshJobCreationService.createJobFromOshScan(scan);

        // Then - should continue processing despite idempotency check error
        // (fail-safe behavior to avoid missing scans)
        verify(jobRepository).findByOshScanId("12345");
        verify(oshJsonDownloadService).downloadSastReport(12345);
    }
}
