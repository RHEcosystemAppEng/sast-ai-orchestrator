package com.redhat.sast.ai.utils;

import com.redhat.sast.ai.dto.TriggerDto;
import com.redhat.sast.ai.model.Workflow;
import com.redhat.sast.ai.model.WorkflowSettings;
import com.redhat.sast.ai.model.WorkflowStatus;

public class ObjectTransformer {

    public static Workflow getWorkflowModel(TriggerDto dto) {

        Workflow workflow = new Workflow();
        workflow.setPackageName(dto.packageName);
        workflow.setOshScanId(dto.oshScanId);
        workflow.setPackageNvr(dto.packageNvr);
        workflow.setSrcUrl(dto.sourceCodeUrl);
        workflow.setInputReportFilePath(dto.inputReportFilePath);
        workflow.setJiraUrl(dto.jiraLink);
        workflow.setProjectName(dto.projectName);
        workflow.setProjectVersion(dto.projectVersion);
        workflow.setActive(1);
        return workflow;
    }

    public static WorkflowSettings getWorkflowSettingsModel(TriggerDto.Settings settings) {

        WorkflowSettings workflowSettings = new WorkflowSettings();
        workflowSettings.setLlmUrl(settings.llmUrl);
        workflowSettings.setLlmModelName(settings.llmModelName);
        workflowSettings.setEmbeddingLlmUrl(settings.embeddingsLlmUrl);
        workflowSettings.setEmbeddingLlmModelName(settings.embeddingsLlmModelName);

        return workflowSettings;
    }

    public static WorkflowStatus getWorkflowStatusModel() {
        WorkflowStatus workflowStatus = new WorkflowStatus();

        return workflowStatus;
    }
}
