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

    @JsonProperty("oshScanId")
    private String oshScanId;

    @JsonProperty("aggregateResultsGSheet")
    private String aggregateResultsGSheet;

    /**
     * Forces a new scan even if a completed scan exists for this NVR.
     * Default is false, meaning cached results will be returned when available.
     */
    @JsonProperty("forceRescan")
    private Boolean forceRescan;

    /**
     * Constructor for URL-based input sources (Google Sheets, SARIF, OSH URLs).
     *
     * @param packageNvr package NVR
     * @param inputSourceUrl URL to the input source (Google Sheet, SARIF file, or OSH report)
     */
    public JobCreationDto(String packageNvr, String inputSourceUrl) {
        this.packageNvr = packageNvr;
        this.inputSourceUrl = inputSourceUrl;
    }
}
