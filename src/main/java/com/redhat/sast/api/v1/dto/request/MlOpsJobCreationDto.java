package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating individual MLOps jobs.
 * Each job has its own packageNvr (the specific package to analyze).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlOpsJobCreationDto {

    @JsonProperty("packageNvr")
    @NotBlank(message = "Package NVR cannot be null or empty.")
    private String packageNvr;

    @JsonProperty("useKnownFalsePositiveFile")
    private Boolean useKnownFalsePositiveFile;

    @JsonProperty("submittedBy")
    private String submittedBy;
}

