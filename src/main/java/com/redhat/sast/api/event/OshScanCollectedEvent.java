package com.redhat.sast.api.event;

import com.redhat.sast.api.model.Job;

/**
 * @param job the newly created job from the collected OSH scan
 */
public record OshScanCollectedEvent(Job job) {}
