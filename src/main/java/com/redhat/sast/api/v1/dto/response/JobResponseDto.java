package com.redhat.sast.api.v1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.model.JobBase;

public class JobResponseDto extends JobBase {

    @JsonProperty("jobId")
    private Long jobId;

    @JsonProperty("sourceCodeUrl")
    private String sourceCodeUrl;

    @JsonProperty("batchId")
    private Long batchId;

    public JobResponseDto() {}

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getSourceCodeUrl() {
        return sourceCodeUrl;
    }

    public void setSourceCodeUrl(String sourceCodeUrl) {
        this.sourceCodeUrl = sourceCodeUrl;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }
}
