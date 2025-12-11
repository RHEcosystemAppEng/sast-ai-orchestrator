package com.redhat.sast.api.common.constants;

/**
 * Constants for DVC (Data Version Control) task result names.
 * These constants define the standard names for task results returned by Tekton pipelines
 * during SAST workflow execution with DVC integration.
 */
public final class DvcTaskResults {

    private DvcTaskResults() {}

    // Core DVC metadata fields (required)
    public static final String DATA_VERSION = "dvc-data-version";
    public static final String COMMIT_HASH = "dvc-commit-hash";
    public static final String PIPELINE_STAGE = "dvc-pipeline-stage";

    // DVC artifact metadata fields
    public static final String HASH = "dvc-hash";
    public static final String PATH = "dvc-path";
    public static final String ARTIFACT_TYPE = "dvc-artifact-type";

    // Analysis and repository metadata fields
    public static final String ANALYSIS_SUMMARY = "dvc-source-analysis-summary";
    public static final String REPO_URL = "dvc-repo-url";
    public static final String REPO_BRANCH = "dvc-repo-branch";

    // Additional workflow metadata fields
    public static final String SPLIT_TYPE = "dvc-split-type";
    public static final String SAST_REPORT_PATH = "dvc-sast-report-path";
    public static final String ISSUES_COUNT = "dvc-issues-count";
    public static final String EXECUTION_TIMESTAMP = "dvc-execution-timestamp";
}
