package com.redhat.sast.api.testdata;

import com.redhat.sast.api.v1.dto.request.JobBatchSubmissionDto;

public class JobBatchTestDataBuilder {

    private String batchGoogleSheetUrl =
            "https://docs.google.com/spreadsheets/d/1wrIcIhC7F9uVf8fm0IlvGTO-dSV9t_maLx36OoRI7S0/edit?usp=sharing";
    private String submittedBy = "test-user";
    private Boolean useKnownFalsePositiveFile = false;

    public static JobBatchTestDataBuilder aBatch() {
        return new JobBatchTestDataBuilder();
    }

    public JobBatchTestDataBuilder withBatchGoogleSheetUrl(String batchGoogleSheetUrl) {
        this.batchGoogleSheetUrl = batchGoogleSheetUrl;
        return this;
    }

    public JobBatchTestDataBuilder withSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
        return this;
    }

    public JobBatchTestDataBuilder withUseKnownFalsePositiveFile(Boolean useKnownFalsePositiveFile) {
        this.useKnownFalsePositiveFile = useKnownFalsePositiveFile;
        return this;
    }

    public JobBatchSubmissionDto build() {
        JobBatchSubmissionDto dto = new JobBatchSubmissionDto(batchGoogleSheetUrl, submittedBy);
        dto.setUseKnownFalsePositiveFile(useKnownFalsePositiveFile);
        return dto;
    }

    public static JobBatchSubmissionDto simpleBatch() {
        return aBatch().build();
    }

    public static JobBatchSubmissionDto firstBatchDataset() {
        return aBatch().withBatchGoogleSheetUrl(
                        "https://docs.google.com/spreadsheets/d/1wrIcIhC7F9uVf8fm0IlvGTO-dSV9t_maLx36OoRI7S0/edit?usp=sharing")
                .withSubmittedBy("integration-test-user-1")
                .build();
    }

    public static JobBatchSubmissionDto secondBatchDataset() {
        return aBatch().withBatchGoogleSheetUrl(
                        "https://docs.google.com/spreadsheets/d/1GcJg8aHfpEGxrPbb2gYD-CadwoxAWEB91Er5q0RpOuE/edit?usp=sharing")
                .withSubmittedBy("integration-test-user-2")
                .build();
    }

    public static JobBatchSubmissionDto batchWithKnownFalsePositives() {
        return aBatch().withUseKnownFalsePositiveFile(true)
                .withSubmittedBy("integration-test-fp-user")
                .build();
    }

    public static JobBatchSubmissionDto batchForUser(String userName) {
        return aBatch().withSubmittedBy(userName).build();
    }
}
