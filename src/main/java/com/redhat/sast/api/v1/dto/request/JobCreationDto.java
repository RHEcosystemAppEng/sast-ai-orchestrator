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

    @JsonProperty("useKnownFalsePositiveFile")
    private Boolean useKnownFalsePositiveFile;

    public JobCreationDto() {}

    public JobCreationDto(String packageNvr, String inputSourceUrl) {
        this.packageNvr = packageNvr;
        this.inputSourceUrl = inputSourceUrl;
    }

    public JobCreationDto(String packageNvr, String inputSourceUrl, Boolean useKnownFalsePositiveFile) {
        this.packageNvr = packageNvr;
        this.inputSourceUrl = inputSourceUrl;
        this.useKnownFalsePositiveFile = useKnownFalsePositiveFile;
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

    public Boolean getUseKnownFalsePositiveFile() {
        return useKnownFalsePositiveFile;
    }

    public void setInputSource(InputSourceDto inputSource) {
        this.inputSource = inputSource;
    }

    public WorkflowSettingsDto getWorkflowSettings() {
        return this.workflowSettings;
    }

    public void setUseKnownFalsePositiveFile(Boolean useKnownFalsePositiveFile) {
        this.useKnownFalsePositiveFile = useKnownFalsePositiveFile;
    }
}
