package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.enums.JobStatus;

public class JobResponseDto {

    @JsonProperty("projectName")
    protected String projectName;

    @JsonProperty("projectVersion")
    protected String projectVersion;

    @JsonProperty("packageName")
    protected String packageName;

    @JsonProperty("packageNvr")
    protected String packageNvr;

    @JsonProperty("oshScanId")
    protected String oshScanId;

    @JsonProperty("jiraLink")
    protected String jiraLink;

    @JsonProperty("hostname")
    protected String hostname;

    @JsonProperty("status")
    protected JobStatus status;

    @JsonProperty("createdAt")
    protected LocalDateTime createdAt;

    @JsonProperty("startedAt")
    protected LocalDateTime startedAt;

    @JsonProperty("completedAt")
    protected LocalDateTime completedAt;

    @JsonProperty("cancelledAt")
    protected LocalDateTime cancelledAt;

    @JsonProperty("tektonUrl")
    protected String tektonUrl;

    @JsonProperty("jobId")
    private Long jobId;

    @JsonProperty("sourceCodeUrl")
    private String sourceCodeUrl;

    @JsonProperty("batchId")
    private Long batchId;

    public JobResponseDto() {}

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getSourceCodeUrl() {
        return sourceCodeUrl;
    }

    public void setSourceCodeUrl(String sourceCodeUrl) {
        this.sourceCodeUrl = sourceCodeUrl;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
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

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getTektonUrl() {
        return tektonUrl;
    }

    public void setTektonUrl(String tektonUrl) {
        this.tektonUrl = tektonUrl;
    }
}
