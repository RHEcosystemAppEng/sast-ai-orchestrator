package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class JobBatchSubmissionDto {

    @JsonProperty("batchGoogleSheetUrl")
    @NotBlank(message = "Batch Google Sheet URL cannot be null or empty.")
    private String batchGoogleSheetUrl;

    @JsonProperty("submittedBy")
    private String submittedBy;

    @JsonProperty("useKnownFalsePositiveFile")
    private Boolean useKnownFalsePositiveFile;

    public JobBatchSubmissionDto() {}

    public JobBatchSubmissionDto(String batchGoogleSheetUrl, String submittedBy) {
        this.batchGoogleSheetUrl = batchGoogleSheetUrl;
        this.submittedBy = submittedBy;
    }

    public JobBatchSubmissionDto(String batchGoogleSheetUrl, String submittedBy, Boolean useKnownFalsePositiveFile) {
        this.batchGoogleSheetUrl = batchGoogleSheetUrl;
        this.submittedBy = submittedBy;
        this.useKnownFalsePositiveFile = useKnownFalsePositiveFile;
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

    public Boolean getUseKnownFalsePositiveFile() {
        return useKnownFalsePositiveFile;
    }

    public void setUseKnownFalsePositiveFile(Boolean useKnownFalsePositiveFile) {
        this.useKnownFalsePositiveFile = useKnownFalsePositiveFile;
    }
}
