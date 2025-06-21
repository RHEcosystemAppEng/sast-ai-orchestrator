package com.redhat.sast.api.v1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class PackageSummaryDto {
    
    @JsonProperty("packageName")
    private String packageName;
    
    @JsonProperty("totalAnalyses")
    private Integer totalAnalyses;
    
    @JsonProperty("lastAnalysisDate")
    private LocalDateTime lastAnalysisDate;
    
    @JsonProperty("completedAnalyses")
    private Integer completedAnalyses;
    
    @JsonProperty("failedAnalyses")
    private Integer failedAnalyses;
    
    @JsonProperty("runningAnalyses")
    private Integer runningAnalyses;

    public PackageSummaryDto() {
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getTotalAnalyses() {
        return totalAnalyses;
    }

    public void setTotalAnalyses(Integer totalAnalyses) {
        this.totalAnalyses = totalAnalyses;
    }

    public LocalDateTime getLastAnalysisDate() {
        return lastAnalysisDate;
    }

    public void setLastAnalysisDate(LocalDateTime lastAnalysisDate) {
        this.lastAnalysisDate = lastAnalysisDate;
    }

    public Integer getCompletedAnalyses() {
        return completedAnalyses;
    }

    public void setCompletedAnalyses(Integer completedAnalyses) {
        this.completedAnalyses = completedAnalyses;
    }

    public Integer getFailedAnalyses() {
        return failedAnalyses;
    }

    public void setFailedAnalyses(Integer failedAnalyses) {
        this.failedAnalyses = failedAnalyses;
    }

    public Integer getRunningAnalyses() {
        return runningAnalyses;
    }

    public void setRunningAnalyses(Integer runningAnalyses) {
        this.runningAnalyses = runningAnalyses;
    }
} 