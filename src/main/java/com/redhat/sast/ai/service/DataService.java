package com.redhat.sast.ai.service;

import com.redhat.sast.ai.dto.TriggerDto;
import com.redhat.sast.ai.dto.WorkflowParamsDto;
import com.redhat.sast.ai.model.Workflow;
import com.redhat.sast.ai.model.WorkflowSettings;
import com.redhat.sast.ai.model.WorkflowStatus;
import com.redhat.sast.ai.repository.WorkflowRepository;
import com.redhat.sast.ai.repository.WorkflowSettingsRepository;
import com.redhat.sast.ai.repository.WorkflowStatusRepository;
import com.redhat.sast.ai.utils.ObjectTransformer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DataService {

    @ConfigProperty(name = "sast.ai.workflow.known.false.positive.base.repo")
    String knownFalsePositivesBaseRepoUrl;

    @Inject
    WorkflowRepository workflowRepository;

    @Inject
    WorkflowSettingsRepository workflowSettingsRepository;

    @Inject
    WorkflowStatusRepository workflowStatusRepository;

    @Transactional
    public Long saveTriggerInfo(TriggerDto triggerDto) {
        Workflow workflow = ObjectTransformer.getWorkflowModel(triggerDto);
        workflowRepository.persist(workflow);

        if (triggerDto.workflowSettings != null) {
            WorkflowSettings workflowSettings = ObjectTransformer.getWorkflowSettingsModel(triggerDto.workflowSettings);
            workflowSettings.setWorkflow(workflow);
            workflowSettingsRepository.persist(workflowSettings);
        }

        WorkflowStatus workflowStatus = ObjectTransformer.getWorkflowStatusModel();
        workflowStatus.setStatus(com.redhat.sast.ai.utils.WorkflowStatus.SCHEDULED);
        workflowStatusRepository.persist(workflowStatus);

        return workflow.getId();
    }

    @Transactional
    public void updateWorkflowStatus(long workflowId, String newStatus) {
        WorkflowStatus status = workflowStatusRepository.find("workflow.id", workflowId).firstResult();
        if (status != null) {
            status.setStatus(newStatus);
            workflowStatusRepository.persist(status);
        }
    }

    public WorkflowParamsDto getWorkflowParams(long workflowId, TriggerDto triggerDto) {

        Workflow workflow = workflowRepository.findById(workflowId);
        if (workflow == null) throw new IllegalArgumentException("Invalid workflow ID (" + workflowId + ")");


        WorkflowParamsDto params = new WorkflowParamsDto();
        params.setSrcCodePath(workflow.getSrcUrl());
        params.setUseKnownFalsePositivesFile(Boolean.TRUE);
        params.setKnownFalsePositivesUrl(getKnownFalsePositivesUrl(workflow.getPackageName()));
        params.setInputReportFilePath(workflow.getInputReportFilePath());
        params.setProjectName(workflow.getProjectName());
        params.setProjectVersion(workflow.getProjectVersion());

        if (StringUtils.isNotEmpty(triggerDto.knownFalsePositivesUrl)) {
            params.setKnownFalsePositivesUrl(triggerDto.knownFalsePositivesUrl);
        } else {
            params.setKnownFalsePositivesUrl(getKnownFalsePositivesUrl(workflow.getPackageName()));
        }

        if (triggerDto.workflowSettings != null) {
            if (StringUtils.isNotEmpty(triggerDto.workflowSettings.llmUrl)) {
                params.setLlmUrl(triggerDto.workflowSettings.llmUrl);
            }
            if (StringUtils.isNotEmpty(triggerDto.workflowSettings.llmModelName)) {
                params.setLlmModelName(triggerDto.workflowSettings.llmModelName);
            }
            if (StringUtils.isNotEmpty(triggerDto.workflowSettings.llmAPIKey)) {
                params.setLlmApiKey(triggerDto.workflowSettings.llmAPIKey);
            }


            if (StringUtils.isNotEmpty(triggerDto.workflowSettings.embeddingsLlmUrl)) {
                params.setEmbeddingsLlmUrl(triggerDto.workflowSettings.embeddingsLlmUrl);
            }
            if (StringUtils.isNotEmpty(triggerDto.workflowSettings.embeddingsLlmModelName)) {
                params.setEmbeddingsLlmModelName(triggerDto.workflowSettings.embeddingsLlmModelName);
            }
            if (StringUtils.isNotEmpty(triggerDto.workflowSettings.embeddingsLlmAPIKey)) {
                params.setEmbeddingsLlmApiKey(triggerDto.workflowSettings.embeddingsLlmAPIKey);
            }

        }
        return params;
    }

    private String getKnownFalsePositivesUrl(String packageName) {
        if (StringUtils.isEmpty(knownFalsePositivesBaseRepoUrl))
            throw new IllegalArgumentException("Known false positives base repository link(" + knownFalsePositivesBaseRepoUrl + ") is not valid.");

        if (knownFalsePositivesBaseRepoUrl.endsWith("/")) {
            return knownFalsePositivesBaseRepoUrl + packageName;
        }

        return knownFalsePositivesBaseRepoUrl + "/" + packageName;
    }
}
