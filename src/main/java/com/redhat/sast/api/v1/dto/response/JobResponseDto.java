package com.redhat.sast.api.v1.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.enums.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDto {

    @JsonProperty("projectName")
    protected String projectName;

    @JsonProperty("projectVersion")
    protected String projectVersion;

    @JsonProperty("packageName")
    protected String packageName;

    @JsonProperty("packageNvr")
    protected String packageNvr;

    @JsonProperty("oshScanId")
    protected String oshScanId;

    @JsonProperty("jiraLink")
    protected String jiraLink;

    @JsonProperty("hostname")
    protected String hostname;

    @JsonProperty("status")
    protected JobStatus status;

    @JsonProperty("createdAt")
    protected Instant createdAt;

    @JsonProperty("startedAt")
    protected Instant startedAt;

    @JsonProperty("completedAt")
    protected Instant completedAt;

    @JsonProperty("cancelledAt")
    protected Instant cancelledAt;

    @JsonProperty("tektonUrl")
    protected String tektonUrl;

    @JsonProperty("jobId")
    private Long jobId;

    @JsonProperty("sourceCodeUrl")
    private String sourceCodeUrl;

    @JsonProperty("batchId")
    private Long batchId;

    @JsonProperty("submittedBy")
    private String submittedBy;

    // =====================================================
    // NVR duplicate detection fields (APPENG-4112)
    // =====================================================

    /**
     * Indicates if this response is from a cached/previously completed scan
     * rather than a newly created job.
     */
    @JsonProperty("isCachedResult")
    private boolean cachedResult;

    /**
     * Indicates if there's already a running scan for this NVR.
     * When true, the response contains info about the existing running job.
     */
    @JsonProperty("isExistingRun")
    private boolean existingRun;

    /**
     * When the original scan was completed (only set for cached results).
     */
    @JsonProperty("originalScanDate")
    private Instant originalScanDate;

    /**
     * Google Sheet URL containing the analysis results (for cached results).
     */
    @JsonProperty("resultGoogleSheetUrl")
    private String resultGoogleSheetUrl;
}
