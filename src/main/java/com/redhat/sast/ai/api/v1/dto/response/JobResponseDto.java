package com.redhat.sast.ai.api.v1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.ai.enums.JobStatus;

import java.time.LocalDateTime;

public class JobResponseDto {
    
    @JsonProperty("jobId")
    private Long jobId;
    
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
    
    @JsonProperty("sourceCodeUrl")
    private String sourceCodeUrl;
    
    @JsonProperty("jiraLink")
    private String jiraLink;
    
    @JsonProperty("hostname")
    private String hostname;
    
    @JsonProperty("status")
    private JobStatus status;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("startedAt")
    private LocalDateTime startedAt;
    
    @JsonProperty("completedAt")
    private LocalDateTime completedAt;
    
    @JsonProperty("tektonUrl")
    private String tektonUrl;
    
    @JsonProperty("batchId")
    private Long batchId;

    public JobResponseDto() {
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
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

    public String getSourceCodeUrl() {
        return sourceCodeUrl;
    }

    public void setSourceCodeUrl(String sourceCodeUrl) {
        this.sourceCodeUrl = sourceCodeUrl;
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

    public String getTektonUrl() {
        return tektonUrl;
    }

    public void setTektonUrl(String tektonUrl) {
        this.tektonUrl = tektonUrl;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }
} 