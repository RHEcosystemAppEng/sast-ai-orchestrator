package com.redhat.sast.api.model;

import java.time.LocalDateTime;

import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.enums.JobStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "job")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name")
    protected String projectName;

    @Column(name = "project_version")
    protected String projectVersion;

    @Column(name = "package_name")
    protected String packageName;

    @Column(name = "package_nvr")
    protected String packageNvr;

    @Column(name = "osh_scan_id")
    protected String oshScanId;

    @Column(name = "package_source_code_url")
    private String packageSourceCodeUrl;

    @Column(name = "jira_link")
    protected String jiraLink;

    @Column(name = "hostname")
    protected String hostname;

    @Column(name = "known_false_positives_url")
    private String knownFalsePositivesUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_source_type")
    private InputSourceType inputSourceType;

    @Column(name = "google_sheet_url")
    private String gSheetUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    protected JobStatus status;

    @Column(name = "created_at", nullable = false)
    protected LocalDateTime createdAt;

    @Column(name = "started_at")
    protected LocalDateTime startedAt;

    @Column(name = "completed_at")
    protected LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    protected LocalDateTime cancelledAt;

    @Column(name = "tekton_url")
    protected String tektonUrl;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_batch_id")
    private JobBatch jobBatch;

    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private JobSettings jobSettings;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = JobStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageSourceCodeUrl() {
        return packageSourceCodeUrl;
    }

    public void setPackageSourceCodeUrl(String packageSourceCodeUrl) {
        this.packageSourceCodeUrl = packageSourceCodeUrl;
    }

    public String getKnownFalsePositivesUrl() {
        return knownFalsePositivesUrl;
    }

    public void setKnownFalsePositivesUrl(String knownFalsePositivesUrl) {
        this.knownFalsePositivesUrl = knownFalsePositivesUrl;
    }

    public InputSourceType getInputSourceType() {
        return inputSourceType;
    }

    public void setInputSourceType(InputSourceType inputSourceType) {
        this.inputSourceType = inputSourceType;
    }

    public String getgSheetUrl() {
        return gSheetUrl;
    }

    public void setGSheetUrl(String gSheetUrl) {
        this.gSheetUrl = gSheetUrl;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public JobBatch getJobBatch() {
        return jobBatch;
    }

    public void setJobBatch(JobBatch jobBatch) {
        this.jobBatch = jobBatch;
    }

    public JobSettings getJobSettings() {
        return jobSettings;
    }

    public void setJobSettings(JobSettings jobSettings) {
        this.jobSettings = jobSettings;
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
