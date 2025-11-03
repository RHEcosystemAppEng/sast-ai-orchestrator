package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for submitting MLOps batch jobs with DVC configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlOpsJobBatchSubmissionDto {

    @JsonProperty("submittedBy")
    private String submittedBy;

    @JsonProperty("useKnownFalsePositiveFile")
    private Boolean useKnownFalsePositiveFile;

    @JsonProperty("dvcNvrVersion")
    @NotBlank(message = "DVC NVR version cannot be null or empty.")
    private String dvcNvrVersion;

    @JsonProperty("dvcKnownFalsePositivesVersion")
    @NotBlank(message = "DVC Known False Positives version cannot be null or empty.")
    private String dvcKnownFalsePositivesVersion;

    @JsonProperty("dvcPromptsVersion")
    @NotBlank(message = "DVC Prompts version cannot be null or empty.")
    private String dvcPromptsVersion;

    @JsonProperty("imageVersion")
    @NotBlank(message = "Image version cannot be null or empty.")
    private String imageVersion;
}

