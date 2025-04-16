package com.redhat.sast.ai.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow")
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package")
    private String packageName;

    @Column(name = "package_nvr")
    private String packageNvr;

    @Column(name = "osh_scan_id")
    private String oshScanId;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "tekton_url")
    private String tektonUrl;

    @Column(name = "src_url")
    private String srcUrl;

    @Column(name = "gsheet_url")
    private String gSheetUrl;

    @Column(name = "jira_url")
    private String jiraUrl;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    private int active;

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public String getTektonUrl() {
        return tektonUrl;
    }

    public void setTektonUrl(String tektonUrl) {
        this.tektonUrl = tektonUrl;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
    }

    public String getgSheetUrl() {
        return gSheetUrl;
    }

    public void setgSheetUrl(String gSheetUrl) {
        this.gSheetUrl = gSheetUrl;
    }

    public String getJiraUrl() {
        return jiraUrl;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }
}
