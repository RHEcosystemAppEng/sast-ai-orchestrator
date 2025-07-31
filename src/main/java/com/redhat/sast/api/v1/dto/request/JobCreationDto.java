package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class JobCreationDto {

    @JsonProperty("packageNvr")
    @NotBlank(message = "Package NVR cannot be null or empty.")
    private String packageNvr;

    @JsonProperty("inputSourceUrl")
    @NotBlank(message = "Input source URL cannot be null or empty.")
    private String inputSourceUrl;

    public JobCreationDto() {}

    public JobCreationDto(String packageNvr, String inputSourceUrl) {
        this.packageNvr = packageNvr;
        this.inputSourceUrl = inputSourceUrl;
    }

    public String getPackageNvr() {
        return packageNvr;
    }

    public void setPackageNvr(String packageNvr) {
        this.packageNvr = packageNvr;
    }

    public String getInputSourceUrl() {
        return inputSourceUrl;
    }

    public void setInputSourceUrl(String inputSourceUrl) {
        this.inputSourceUrl = inputSourceUrl;
    }
}
