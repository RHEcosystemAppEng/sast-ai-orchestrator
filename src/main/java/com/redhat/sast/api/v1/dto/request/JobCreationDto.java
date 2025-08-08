package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCreationDto {

    @JsonProperty("packageNvr")
    @NotBlank(message = "Package NVR cannot be null or empty.")
    private String packageNvr;

    @JsonProperty("inputSourceUrl")
    @NotBlank(message = "Input source URL cannot be null or empty.")
    private String inputSourceUrl;

    @JsonProperty("useKnownFalsePositiveFile")
    private Boolean useKnownFalsePositiveFile;

    @JsonProperty("submittedBy")
    private String submittedBy;

    public JobCreationDto(String packageNvr, String inputSourceUrl) {
        this.packageNvr = packageNvr;
        this.inputSourceUrl = inputSourceUrl;
    }
}
