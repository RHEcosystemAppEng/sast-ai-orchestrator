package com.redhat.sast.api.repository;

import com.redhat.sast.api.model.JobBatch;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobBatchRepository implements PanacheRepository<JobBatch> {}
