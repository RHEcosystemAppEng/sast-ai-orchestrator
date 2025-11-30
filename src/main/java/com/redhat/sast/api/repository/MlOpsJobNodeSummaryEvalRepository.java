package com.redhat.sast.api.repository;

import com.redhat.sast.api.model.MlOpsJobNodeSummaryEval;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsJobNodeSummaryEvalRepository implements PanacheRepositoryBase<MlOpsJobNodeSummaryEval, Long> {}
