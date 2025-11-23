package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlOpsBatchSubmissionDto {

    @JsonProperty("testingDataNvrsVersion")
    @NotBlank(message = "Testing data NVRs version cannot be null or empty.")
    private String testingDataNvrsVersion;

    @JsonProperty("promptsVersion")
    @NotBlank(message = "Prompts version cannot be null or empty.")
    private String promptsVersion;

    @JsonProperty("knownNonIssuesVersion")
    @NotBlank(message = "Known non-issues version cannot be null or empty.")
    private String knownNonIssuesVersion;

    @JsonProperty("sastAiImage")
    @NotBlank(message = "SAST AI image cannot be null or empty.")
    private String sastAiImage;

    @JsonProperty("submittedBy")
    private String submittedBy;

    @JsonProperty("jobSettings")
    private JobSettingsDto jobSettings;
}
