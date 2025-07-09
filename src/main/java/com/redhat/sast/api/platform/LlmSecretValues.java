package com.redhat.sast.api.platform;

/**
 * Record holding LLM-related secret values read from OpenShift secrets.
 * All values are guaranteed to be non-null (empty string if not found).
 */
public record LlmSecretValues(
        String llmUrl,
        String llmApiKey,
        String embeddingsUrl,
        String embeddingsApiKey,
        String llmModelName,
        String embeddingsModelName) {

    /**
     * Constructor that ensures all values are non-null.
     */
    public LlmSecretValues {
        llmUrl = llmUrl != null ? llmUrl : "";
        llmApiKey = llmApiKey != null ? llmApiKey : "";
        embeddingsUrl = embeddingsUrl != null ? embeddingsUrl : "";
        embeddingsApiKey = embeddingsApiKey != null ? embeddingsApiKey : "";
        llmModelName = llmModelName != null ? llmModelName : "";
        embeddingsModelName = embeddingsModelName != null ? embeddingsModelName : "";
    }

    /**
     * Creates a SecretValues with all empty values.
     */
    public static LlmSecretValues empty() {
        return new LlmSecretValues("", "", "", "", "", "");
    }
}
