package com.redhat.sast.ai;

import com.redhat.sast.ai.dto.TriggerDto;
import com.redhat.sast.ai.service.WorkflowService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/workflow")
public class WorkflowResource {

    @Inject
    WorkflowService workflowService;

    @Path("/start")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String startNewWorkflow(TriggerDto params) {
        workflowService.initSastAiWorkflow(params);
        return "Successfully initiated SAST-AI-Workflow";
    }

    @Path("/start")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String test() {
        return "dummy endpoint";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String health() {
        return "Healthy";
    }
}
