package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSettingsDto {

    @JsonProperty("llmUrl")
    private String llmUrl;

    @JsonProperty("llmModelName")
    private String llmModelName;

    @JsonProperty("llmApiType")
    private String llmApiType;

    @JsonProperty("llmApiKey")
    private String llmApiKey;

    @JsonProperty("embeddingLlmUrl")
    private String embeddingLlmUrl;

    @JsonProperty("embeddingLlmModelName")
    private String embeddingLlmModelName;

    @JsonProperty("embeddingLlmApiKey")
    private String embeddingLlmApiKey;

    @JsonProperty("secretName")
    private String secretName;

    @JsonProperty("useKnownFalsePositiveFile")
    private Boolean useKnownFalsePositiveFile;

    @JsonProperty("evaluateSpecificNode")
    private String evaluateSpecificNode;
}
