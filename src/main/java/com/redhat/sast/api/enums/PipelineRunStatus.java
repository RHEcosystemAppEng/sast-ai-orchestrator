package com.redhat.sast.api.enums;

/**
 * Represents the status of a Tekton PipelineRun for recovery decisions.
 */
public enum PipelineRunStatus {
    NOT_FOUND,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    ERROR
}
