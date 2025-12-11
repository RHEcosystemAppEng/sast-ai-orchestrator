package com.redhat.sast.api.repository.mlops;

import com.redhat.sast.api.model.MlOpsJobFinding;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsJobFindingRepository implements PanacheRepositoryBase<MlOpsJobFinding, Long> {}
