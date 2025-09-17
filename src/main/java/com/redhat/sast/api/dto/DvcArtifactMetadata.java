package com.redhat.sast.api.dto;

/**
 * DTO for DVC artifact metadata
 */
public class DvcArtifactMetadata {
    private final Long jobId;
    private final String version;
    private final String dvcHash;
    private final String dvcPath;
    private final String artifactType;
    private final String analysisSummary;
    private final String repoUrl;
    private final String repoBranch;
    private final String splitType;
    private final String sastReportPath;
    private final String issuesCount;

    public DvcArtifactMetadata(
            Long jobId,
            String version,
            String dvcHash,
            String dvcPath,
            String artifactType,
            String analysisSummary,
            String repoUrl,
            String repoBranch,
            String splitType,
            String sastReportPath,
            String issuesCount) {
        this.jobId = jobId;
        this.version = version;
        this.dvcHash = dvcHash;
        this.dvcPath = dvcPath;
        this.artifactType = artifactType;
        this.analysisSummary = analysisSummary;
        this.repoUrl = repoUrl;
        this.repoBranch = repoBranch;
        this.splitType = splitType;
        this.sastReportPath = sastReportPath;
        this.issuesCount = issuesCount;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getVersion() {
        return version;
    }

    public String getDvcHash() {
        return dvcHash;
    }

    public String getDvcPath() {
        return dvcPath;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public String getAnalysisSummary() {
        return analysisSummary;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public String getRepoBranch() {
        return repoBranch;
    }

    public String getSplitType() {
        return splitType;
    }

    public String getSastReportPath() {
        return sastReportPath;
    }

    public String getIssuesCount() {
        return issuesCount;
    }
}
