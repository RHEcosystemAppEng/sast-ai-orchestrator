package com.redhat.sast.api.service;

import com.redhat.sast.api.v1.dto.request.JobBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.repository.JobBatchRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class JobBatchService {

    @Inject
    JobBatchRepository jobBatchRepository;

    @Transactional
    public JobBatchResponseDto submitBatch(JobBatchSubmissionDto submissionDto) {
        JobBatch batch = new JobBatch();
        batch.setSourceUrl(submissionDto.getSourceUrl());
        batch.setSubmittedBy(submissionDto.getSubmittedBy());
        
        jobBatchRepository.persist(batch);
        
        // TODO: Implement Google Sheets parsing logic here
        // This would parse the sheet and create individual jobs
        
        return convertToResponseDto(batch);
    }

    public List<JobBatchResponseDto> getAllBatches(int page, int size) {
        return jobBatchRepository.findAll()
            .page(Page.of(page, size))
            .list()
            .stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
    }

    public JobBatchResponseDto getBatchById(Long batchId) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("Batch not found with id: " + batchId);
        }
        return convertToResponseDto(batch);
    }

    @Transactional
    public void updateBatchStatus(Long batchId, String status) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setStatus(status);
            jobBatchRepository.persist(batch);
        }
    }

    private JobBatchResponseDto convertToResponseDto(JobBatch batch) {
        JobBatchResponseDto dto = new JobBatchResponseDto();
        dto.setBatchId(batch.getId());
        dto.setSourceUrl(batch.getSourceUrl());
        dto.setSubmittedBy(batch.getSubmittedBy());
        dto.setSubmittedAt(batch.getSubmittedAt());
        dto.setStatus(batch.getStatus());
        dto.setTotalJobs(batch.getTotalJobs());
        dto.setCompletedJobs(batch.getCompletedJobs());
        dto.setFailedJobs(batch.getFailedJobs());
        return dto;
    }
} 