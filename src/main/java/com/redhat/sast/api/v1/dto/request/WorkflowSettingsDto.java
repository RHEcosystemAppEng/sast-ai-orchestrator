package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowSettingsDto {

    @JsonProperty("llmModelName")
    private String llmModelName;

    @JsonProperty("embeddingsLlmModelName")
    private String embeddingsLlmModelName;

    @JsonProperty("secretName")
    private String secretName;

    @JsonProperty("useKnownFalsePositiveFile")
    private Boolean useKnownFalsePositiveFile;

    // Custom constructor for partial initialization (keeping existing logic)
    public WorkflowSettingsDto(String llmModelName, String embeddingsLlmModelName, String secretName) {
        this.llmModelName = llmModelName;
        this.embeddingsLlmModelName = embeddingsLlmModelName;
        this.secretName = secretName;
    }
}
