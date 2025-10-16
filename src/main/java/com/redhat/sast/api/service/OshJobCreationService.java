package com.redhat.sast.api.service;

import java.util.Optional;

import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.v1.dto.osh.OshScanResponse;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for creating SAST-AI workflow jobs from OSH (Open Scan Hub) scan results.
 *
 * This service handles the complete flow from OSH scan discovery to job creation:
 * 1. Downloads JSON content from OSH logs
 * 2. Extracts package NVR from OSH scan metadata
 * 3. Creates jobs via existing JobService infrastructure
 * 4. Ensures idempotency (no duplicate processing)
 *
 * Transaction Boundaries:
 * - Each scan is processed in a separate REQUIRES_NEW transaction
 * - Failed individual scans don't affect other scans in the batch
 * - Idempotency checks prevent duplicate job creation
 */
@ApplicationScoped
@Slf4j
public class OshJobCreationService {

    @Inject
    JobService jobService;

    @Inject
    OshJsonDownloadService oshJsonDownloadService;

    @Inject
    JobRepository jobRepository;

    /**
     * Creates a SAST-AI workflow job from an OSH scan result.
     *
     * Process:
     * 1. Check if scan already processed
     * 2. Download JSON content from OSH logs
     * 3. Build package NVR from scan metadata
     * 4. Create job via JobService with JSON content
     *
     * @param scan OSH scan response containing scan metadata
     * @return Created Job if successful, empty if skipped or failed
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<Job> createJobFromOshScan(OshScanResponse scan) {
        LOGGER.debug("Processing OSH scan ID: {} for package: {}", scan.getScanId(), scan.getPackageName());

        try {
            if (isAlreadyProcessed(scan.getScanId())) {
                LOGGER.debug("OSH scan {} already processed, skipping", scan.getScanId());
                return Optional.empty();
            }

            Optional<String> jsonContent = oshJsonDownloadService.downloadSastReport(scan.getScanId());
            if (jsonContent.isEmpty()) {
                LOGGER.warn("No SAST report JSON available for OSH scan {}, cannot create job", scan.getScanId());
                return Optional.empty();
            }

            String packageNvr = buildNvrFromScan(scan);

            JobCreationDto dto = new JobCreationDto(
                    packageNvr, jsonContent.get(), scan.getScanId().toString());
            dto.setSubmittedBy("OSH_SCHEDULER");

            Job job = jobService.createJobEntity(dto);

            LOGGER.info(
                    "Successfully created job {} from OSH scan {} for package {}",
                    job.getId(),
                    scan.getScanId(),
                    packageNvr);

            return Optional.of(job);

        } catch (Exception e) {
            LOGGER.error("Failed to create job from OSH scan {}: {}", scan.getScanId(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Checks if an OSH scan has already been processed to prevent duplicate job creation.
     *
     * @param scanId OSH scan ID
     * @return true if scan already processed, false otherwise
     */
    private boolean isAlreadyProcessed(Integer scanId) {
        if (scanId == null) {
            return false;
        }

        try {
            Optional<Job> existingJob = jobRepository.findByOshScanId(scanId.toString());
            return existingJob.isPresent();
        } catch (Exception e) {
            LOGGER.warn("Error checking if OSH scan {} already processed: {}", scanId, e.getMessage());
            // Fail safe - assume not processed to avoid missing scans
            return false;
        }
    }

    /**
     * Builds package NVR (Name-Version-Release) from OSH scan metadata.
     *
     * OSH scans provide component and version information extracted from the "Label" field.
     * If the original Label contains full NVR (e.g., "systemd-252-54.el9.src.rpm"),
     * we try to extract it from rawData. Otherwise, we use component-version format.
     *
     * @param scan OSH scan response
     * @return NVR string extracted from OSH data
     */
    private String buildNvrFromScan(OshScanResponse scan) {
        String component = scan.getComponent();
        String version = scan.getVersion();

        if (component == null || component.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "OSH scan " + scan.getScanId() + " missing component name, cannot build NVR");
        }

        String originalLabel = extractOriginalLabel(scan);
        if (originalLabel != null && originalLabel.contains(component)) {
            String nvr = extractNvrFromLabel(originalLabel);
            if (nvr != null) {
                LOGGER.debug(
                        "Extracted NVR '{}' from OSH label '{}' for scan {}", nvr, originalLabel, scan.getScanId());
                return nvr;
            }
        }

        // Fallback: construct from component and version only
        if (version == null || version.trim().isEmpty()) {
            LOGGER.warn("OSH scan {} missing version, using component name only", scan.getScanId());
            return component;
        }

        String nvr = String.format("%s-%s", component, version);
        LOGGER.debug(
                "Built NVR '{}' from OSH scan {} (component: {}, version: {})",
                nvr,
                scan.getScanId(),
                component,
                version);

        return nvr;
    }

    /**
     * Extracts the original label field from OSH rawData if available.
     */
    private String extractOriginalLabel(OshScanResponse scan) {
        if (scan.getRawData() == null) {
            return null;
        }

        Object label = scan.getRawData().get("Label");
        if (label != null) {
            return label.toString();
        }

        return null;
    }

    /**
     * Extracts NVR from RPM-style label (e.g., "systemd-252-54.el9.src.rpm" → "systemd-252-54.el9").
     */
    private String extractNvrFromLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            return null;
        }

        String cleaned = label.replaceAll("\\.src\\.rpm$", "");

        cleaned = cleaned.replaceAll("\\.rpm$", "");

        return cleaned.trim();
    }

    /**
     * Validates that an OSH scan is eligible for job creation.
     *
     * Checks:
     * - Scan state is CLOSED (completed successfully)
     * - Component name is available
     * - Scan ID is valid
     *
     * @param scan OSH scan response
     * @return true if scan can be processed, false otherwise
     */
    public boolean canProcessScan(OshScanResponse scan) {
        if (scan == null) {
            LOGGER.debug("Null scan cannot be processed");
            return false;
        }

        if (scan.getScanId() == null) {
            LOGGER.debug("Scan with null ID cannot be processed");
            return false;
        }

        if (!"CLOSED".equals(scan.getState())) {
            LOGGER.debug(
                    "Scan {} has state '{}', only CLOSED scans can be processed", scan.getScanId(), scan.getState());
            return false;
        }

        if (scan.getComponent() == null || scan.getComponent().trim().isEmpty()) {
            LOGGER.debug("Scan {} missing component name, cannot process", scan.getScanId());
            return false;
        }

        return true;
    }
}
