package com.redhat.sast.ai.service;

import com.redhat.sast.ai.dto.TriggerDto;
import com.redhat.sast.ai.dto.WorkflowParamsDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WorkflowService {


    @Inject
    DataService dataService;

    @Inject
    PlatformService platformService;

    public void initSastAiWorkflow(TriggerDto triggerDto){
        Long workflowId = dataService.saveTriggerInfo(triggerDto);
        WorkflowParamsDto workflowParams = dataService.getWorkflowParams(workflowId, triggerDto);

        platformService.startSastAIWorkflow(workflowParams);
    }
}
