package com.redhat.sast.api.repository;

import com.redhat.sast.api.model.MlOpsJobNodeFilterEval;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsJobNodeFilterEvalRepository implements PanacheRepositoryBase<MlOpsJobNodeFilterEval, Long> {}
