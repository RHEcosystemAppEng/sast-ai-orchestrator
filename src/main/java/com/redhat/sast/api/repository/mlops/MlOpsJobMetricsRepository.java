package com.redhat.sast.api.repository.mlops;

import com.redhat.sast.api.model.MlOpsJobMetrics;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsJobMetricsRepository implements PanacheRepository<MlOpsJobMetrics> {}
