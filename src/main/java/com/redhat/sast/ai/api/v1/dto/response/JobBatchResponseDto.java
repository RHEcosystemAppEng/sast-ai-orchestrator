package com.redhat.sast.ai.api.v1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class JobBatchResponseDto {
    
    @JsonProperty("batchId")
    private Long batchId;
    
    @JsonProperty("sourceUrl")
    private String sourceUrl;
    
    @JsonProperty("submittedBy")
    private String submittedBy;
    
    @JsonProperty("submittedAt")
    private LocalDateTime submittedAt;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("totalJobs")
    private Integer totalJobs;
    
    @JsonProperty("completedJobs")
    private Integer completedJobs;
    
    @JsonProperty("failedJobs")
    private Integer failedJobs;

    public JobBatchResponseDto() {
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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