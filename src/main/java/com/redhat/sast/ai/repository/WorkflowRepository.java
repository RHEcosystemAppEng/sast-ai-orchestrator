package com.redhat.sast.ai.repository;

import com.redhat.sast.ai.model.Workflow;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WorkflowRepository implements PanacheRepository<Workflow> {
}
