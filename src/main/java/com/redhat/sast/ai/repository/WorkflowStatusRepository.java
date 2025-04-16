package com.redhat.sast.ai.repository;

import com.redhat.sast.ai.model.WorkflowStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WorkflowStatusRepository implements PanacheRepository<WorkflowStatus> {
}
