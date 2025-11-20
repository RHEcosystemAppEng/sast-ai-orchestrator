package com.redhat.sast.api.repository;

import com.redhat.sast.api.model.MlOpsJob;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsJobRepository implements PanacheRepository<MlOpsJob> {}
