package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.enums.BatchStatus;

public class JobBatchResponseDto {

    @JsonProperty("batchId")
    private Long batchId;

    @JsonProperty("batchGoogleSheetUrl")
    private String batchGoogleSheetUrl;

    @JsonProperty("submittedBy")
    private String submittedBy;

    @JsonProperty("submittedAt")
    private LocalDateTime submittedAt;

    @JsonProperty("status")
    private BatchStatus status;

    @JsonProperty("totalJobs")
    private Integer totalJobs;

    @JsonProperty("completedJobs")
    private Integer completedJobs;

    @JsonProperty("failedJobs")
    private Integer failedJobs;

    public JobBatchResponseDto() {}

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getBatchGoogleSheetUrl() {
        return batchGoogleSheetUrl;
    }

    public void setBatchGoogleSheetUrl(String batchGoogleSheetUrl) {
        this.batchGoogleSheetUrl = batchGoogleSheetUrl;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public Integer getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(Integer totalJobs) {
        this.totalJobs = totalJobs;
    }

    public Integer getCompletedJobs() {
        return completedJobs;
    }

    public void setCompletedJobs(Integer completedJobs) {
        this.completedJobs = completedJobs;
    }

    public Integer getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(Integer failedJobs) {
        this.failedJobs = failedJobs;
    }
}
