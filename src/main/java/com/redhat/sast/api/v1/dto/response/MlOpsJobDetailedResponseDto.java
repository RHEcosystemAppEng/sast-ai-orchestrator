package com.redhat.sast.api.v1.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.enums.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlOpsJobDetailedResponseDto {

    @JsonProperty("jobId")
    private Long jobId;

    @JsonProperty("packageNvr")
    private String packageNvr;

    @JsonProperty("packageName")
    private String packageName;

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("projectVersion")
    private String projectVersion;

    @JsonProperty("packageSourceCodeUrl")
    private String packageSourceCodeUrl;

    @JsonProperty("knownFalsePositivesUrl")
    private String knownFalsePositivesUrl;

    @JsonProperty("status")
    private JobStatus status;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("startedAt")
    private LocalDateTime startedAt;

    @JsonProperty("completedAt")
    private LocalDateTime completedAt;

    @JsonProperty("tektonUrl")
    private String tektonUrl;

    @JsonProperty("oshScanId")
    private String oshScanId;

    @JsonProperty("metrics")
    private MlOpsJobMetricsDto metrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MlOpsJobMetricsDto {

        @JsonProperty("accuracy")
        private BigDecimal accuracy;

        @JsonProperty("precision")
        private BigDecimal precision;

        @JsonProperty("recall")
        private BigDecimal recall;

        @JsonProperty("f1Score")
        private BigDecimal f1Score;

        @JsonProperty("confusionMatrix")
        private ConfusionMatrixDto confusionMatrix;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfusionMatrixDto {

        @JsonProperty("tp")
        private Integer tp; // True Positives

        @JsonProperty("fp")
        private Integer fp; // False Positives

        @JsonProperty("tn")
        private Integer tn; // True Negatives

        @JsonProperty("fn")
        private Integer fn; // False Negatives
    }
}
