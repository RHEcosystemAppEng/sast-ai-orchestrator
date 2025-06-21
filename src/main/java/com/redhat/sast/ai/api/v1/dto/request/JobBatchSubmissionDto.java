package com.redhat.sast.ai.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobBatchSubmissionDto {
    
    @JsonProperty("sourceUrl")
    private String sourceUrl;
    
    @JsonProperty("submittedBy")
    private String submittedBy;

    public JobBatchSubmissionDto() {
    }

    public JobBatchSubmissionDto(String sourceUrl, String submittedBy) {
        this.sourceUrl = sourceUrl;
        this.submittedBy = submittedBy;
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
} 