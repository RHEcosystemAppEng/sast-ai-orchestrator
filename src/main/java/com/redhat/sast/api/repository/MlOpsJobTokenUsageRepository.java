package com.redhat.sast.api.repository;

import com.redhat.sast.api.model.MlOpsJobTokenUsage;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsJobTokenUsageRepository implements PanacheRepositoryBase<MlOpsJobTokenUsage, Long> {}
