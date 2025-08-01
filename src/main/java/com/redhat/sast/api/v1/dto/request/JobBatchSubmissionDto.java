package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobBatchSubmissionDto {

    @JsonProperty("batchGoogleSheetUrl")
    @NotBlank(message = "Batch Google Sheet URL cannot be null or empty.")
    private String batchGoogleSheetUrl;

    @JsonProperty("submittedBy")
    private String submittedBy;

    @JsonProperty("useKnownFalsePositiveFile")
    private Boolean useKnownFalsePositiveFile;

    public JobBatchSubmissionDto(String batchGoogleSheetUrl, String submittedBy) {
        this.batchGoogleSheetUrl = batchGoogleSheetUrl;
        this.submittedBy = submittedBy;
    }
}
