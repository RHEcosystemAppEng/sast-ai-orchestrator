package com.redhat.sast.api.common.constants;

/**
 * Constants for Tekton pipeline names.
 */
public final class PipelineConstants {

    private PipelineConstants() {
        // Utility class
    }

    /** Standard SAST AI workflow pipeline */
    public static final String SAST_AI_PIPELINE = "sast-ai-workflow-pipeline";

    /** MLOps workflow pipeline with DVC integration */
    public static final String MLOPS_PIPELINE = "sast-ai-workflow-pipeline-mlops";
}

