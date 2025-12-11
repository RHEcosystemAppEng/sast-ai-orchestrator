package com.redhat.sast.api.repository.mlops;

import com.redhat.sast.api.model.MlOpsJobNodeJudgeEval;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsJobNodeJudgeEvalRepository implements PanacheRepositoryBase<MlOpsJobNodeJudgeEval, Long> {}
