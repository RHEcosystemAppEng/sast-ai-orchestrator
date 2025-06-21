package com.redhat.sast.api.repository;

import com.redhat.sast.api.model.JobSettings;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobSettingsRepository implements PanacheRepository<JobSettings> {}
