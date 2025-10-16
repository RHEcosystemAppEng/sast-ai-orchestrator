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
    private String inputSourceUrl;

    @JsonProperty("useKnownFalsePositiveFile")
    private Boolean useKnownFalsePositiveFile;

    @JsonProperty("submittedBy")
    private String submittedBy;

    @JsonProperty("jsonContent")
    private String jsonContent;

    @JsonProperty("oshScanId")
    private String oshScanId;

    public JobCreationDto(String packageNvr, String inputSourceUrl) {
        this.packageNvr = packageNvr;
        this.inputSourceUrl = inputSourceUrl;
    }

    /**
     * Constructor for OSH scan jobs with JSON content.
     * Sets inputSourceUrl to empty string since OSH jobs don't use URL-based input.
     *
     * @param packageNvr NVR extracted from OSH scan metadata
     * @param jsonContent JSON content downloaded from OSH logs
     * @param oshScanId OSH scan ID for traceability
     */
    public JobCreationDto(String packageNvr, String jsonContent, String oshScanId) {
        this.packageNvr = packageNvr;
        this.inputSourceUrl = "";
        this.jsonContent = jsonContent;
        this.oshScanId = oshScanId;
    }
}
