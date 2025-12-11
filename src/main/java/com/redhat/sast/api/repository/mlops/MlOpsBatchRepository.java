package com.redhat.sast.api.repository.mlops;

import com.redhat.sast.api.model.MlOpsBatch;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsBatchRepository implements PanacheRepository<MlOpsBatch> {}
