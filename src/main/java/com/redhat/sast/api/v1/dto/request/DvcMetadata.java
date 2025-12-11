package com.redhat.sast.api.v1.dto.request;

/**
 * DTO for DVC artifact metadata
 */
public class DvcMetadata {
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

    private DvcMetadata(Builder builder) {
        this.jobId = builder.jobId;
        this.version = builder.version;
        this.dvcHash = builder.dvcHash;
        this.dvcPath = builder.dvcPath;
        this.artifactType = builder.artifactType;
        this.analysisSummary = builder.analysisSummary;
        this.repoUrl = builder.repoUrl;
        this.repoBranch = builder.repoBranch;
        this.splitType = builder.splitType;
        this.sastReportPath = builder.sastReportPath;
        this.issuesCount = builder.issuesCount;
    }

    public static Builder builder() {
        return new Builder();
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

    public static class Builder {
        private Long jobId;
        private String version;
        private String dvcHash;
        private String dvcPath;
        private String artifactType;
        private String analysisSummary;
        private String repoUrl;
        private String repoBranch;
        private String splitType;
        private String sastReportPath;
        private String issuesCount;

        public Builder jobId(Long jobId) {
            this.jobId = jobId;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder dvcHash(String dvcHash) {
            this.dvcHash = dvcHash;
            return this;
        }

        public Builder dvcPath(String dvcPath) {
            this.dvcPath = dvcPath;
            return this;
        }

        public Builder artifactType(String artifactType) {
            this.artifactType = artifactType;
            return this;
        }

        public Builder analysisSummary(String analysisSummary) {
            this.analysisSummary = analysisSummary;
            return this;
        }

        public Builder repoUrl(String repoUrl) {
            this.repoUrl = repoUrl;
            return this;
        }

        public Builder repoBranch(String repoBranch) {
            this.repoBranch = repoBranch;
            return this;
        }

        public Builder splitType(String splitType) {
            this.splitType = splitType;
            return this;
        }

        public Builder sastReportPath(String sastReportPath) {
            this.sastReportPath = sastReportPath;
            return this;
        }

        public Builder issuesCount(String issuesCount) {
            this.issuesCount = issuesCount;
            return this;
        }

        public DvcMetadata build() {
            return new DvcMetadata(this);
        }
    }
}
