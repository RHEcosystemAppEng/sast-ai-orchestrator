package com.redhat.sast.api.event;

import com.redhat.sast.api.model.Job;

/**
 * @param job the job that changed status
 */
public record JobStatusChangedEvent(Job job) {}
