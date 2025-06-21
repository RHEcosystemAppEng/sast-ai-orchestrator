package com.redhat.sast.ai.platform;

/**
 * Record holding LLM-related secret values read from OpenShift secrets.
 * All values are guaranteed to be non-null (empty string if not found).
 */
public record LlmSecretValues(
    String llmUrl,
    String llmApiKey,
    String embeddingsUrl,
    String embeddingsApiKey
) {
    
    /**
     * Constructor that ensures all values are non-null.
     */
    public LlmSecretValues {
        llmUrl = llmUrl != null ? llmUrl : "";
        llmApiKey = llmApiKey != null ? llmApiKey : "";
        embeddingsUrl = embeddingsUrl != null ? embeddingsUrl : "";
        embeddingsApiKey = embeddingsApiKey != null ? embeddingsApiKey : "";
    }
    
    /**
     * Creates a SecretValues with all empty values.
     */
    public static LlmSecretValues empty() {
        return new LlmSecretValues("", "", "", "");
    }
} 