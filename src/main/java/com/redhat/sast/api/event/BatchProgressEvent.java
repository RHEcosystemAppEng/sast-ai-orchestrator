package com.redhat.sast.api.event;

import com.redhat.sast.api.model.JobBatch;

/**
 * @param jobBatch the batch with updated progress
 */
public record BatchProgressEvent(JobBatch jobBatch) {}
