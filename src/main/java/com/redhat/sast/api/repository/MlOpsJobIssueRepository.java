package com.redhat.sast.api.repository;

import com.redhat.sast.api.model.MlOpsJobIssue;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsJobIssueRepository implements PanacheRepositoryBase<MlOpsJobIssue, Long> {}
