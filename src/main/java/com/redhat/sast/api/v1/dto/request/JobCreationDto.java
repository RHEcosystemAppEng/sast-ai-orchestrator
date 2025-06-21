package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobCreationDto {
    
    @JsonProperty("projectName")
    private String projectName;
    
    @JsonProperty("projectVersion")
    private String projectVersion;
    
    @JsonProperty("packageName")
    private String packageName;
    
    @JsonProperty("packageNvr")
    private String packageNvr;
    
    @JsonProperty("oshScanId")
    private String oshScanId;
    
    @JsonProperty("packageSourceCodeUrl")
    private String packageSourceCodeUrl;
    
    @JsonProperty("jiraLink")
    private String jiraLink;
    
    @JsonProperty("hostname")
    private String hostname;
    
    @JsonProperty("knownFalsePositivesUrl")
    private String knownFalsePositivesUrl;
    
    @JsonProperty("inputSource")
    private InputSourceDto inputSource;
    
    @JsonProperty("workflowSettings")
    private WorkflowSettingsDto workflowSettings;

    public JobCreationDto() {
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageNvr() {
        return packageNvr;
    }

    public void setPackageNvr(String packageNvr) {
        this.packageNvr = packageNvr;
    }

    public String getOshScanId() {
        return oshScanId;
    }

    public void setOshScanId(String oshScanId) {
        this.oshScanId = oshScanId;
    }

    public String getPackageSourceCodeUrl() {
        return packageSourceCodeUrl;
    }

    public void setPackageSourceCodeUrl(String packageSourceCodeUrl) {
        this.packageSourceCodeUrl = packageSourceCodeUrl;
    }

    public String getJiraLink() {
        return jiraLink;
    }

    public void setJiraLink(String jiraLink) {
        this.jiraLink = jiraLink;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getKnownFalsePositivesUrl() {
        return knownFalsePositivesUrl;
    }

    public void setKnownFalsePositivesUrl(String knownFalsePositivesUrl) {
        this.knownFalsePositivesUrl = knownFalsePositivesUrl;
    }

    public InputSourceDto getInputSource() {
        return inputSource;
    }

    public void setInputSource(InputSourceDto inputSource) {
        this.inputSource = inputSource;
    }

    public WorkflowSettingsDto getWorkflowSettings() {
        return workflowSettings;
    }

    public void setWorkflowSettings(WorkflowSettingsDto workflowSettings) {
        this.workflowSettings = workflowSettings;
    }
} 