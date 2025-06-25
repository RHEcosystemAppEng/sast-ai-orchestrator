package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowSettingsDto {

    @JsonProperty("llmModelName")
    private String llmModelName;

    @JsonProperty("embeddingsLlmModelName")
    private String embeddingsLlmModelName;

    @JsonProperty("secretName")
    private String secretName;

    public WorkflowSettingsDto() {}

    public WorkflowSettingsDto(String llmModelName, String embeddingsLlmModelName, String secretName) {
        this.llmModelName = llmModelName;
        this.embeddingsLlmModelName = embeddingsLlmModelName;
        this.secretName = secretName;
    }

    public WorkflowSettingsDto(WorkflowSettingsDto other) {
        // Add field-by-field copy here as appropriate
        // Example: this.someField = other.someField;
        // If there are mutable fields, copy them defensively
    }

    public String getLlmModelName() {
        return llmModelName;
    }

    public void setLlmModelName(String llmModelName) {
        this.llmModelName = llmModelName;
    }

    public String getEmbeddingsLlmModelName() {
        return embeddingsLlmModelName;
    }

    public void setEmbeddingsLlmModelName(String embeddingsLlmModelName) {
        this.embeddingsLlmModelName = embeddingsLlmModelName;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }
}
