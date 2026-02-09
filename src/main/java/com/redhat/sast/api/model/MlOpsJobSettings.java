package com.redhat.sast.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "mlops_job_settings",
        indexes = {@Index(name = "idx_mlops_job_settings_id", columnList = "id")})
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "mlOpsJob")
public class MlOpsJobSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "mlops_job_id")
    private MlOpsJob mlOpsJob;

    @Column(name = "llm_url")
    private String llmUrl;

    @Column(name = "llm_model_name")
    private String llmModelName;

    /**
     * LLM API provider type (e.g., "openai", "nim", "azure", "anthropic", "google", "bedrock").
     * Falls back to secret value if not specified, or defaults to "openai" if both are empty.
     */
    @Column(name = "llm_api_type")
    private String llmApiType;

    @Transient
    private String llmApiKey;

    @Column(name = "embedding_llm_url")
    private String embeddingLlmUrl;

    @Column(name = "embedding_llm_model_name")
    private String embeddingLlmModelName;

    @Transient
    private String embeddingLlmApiKey;

    @Column(name = "secret_name")
    private String secretName;

    @Column(name = "use_known_false_positive_file")
    private Boolean useKnownFalsePositiveFile;

    @Column(name = "evaluate_specific_node", length = 100)
    private String evaluateSpecificNode;
}
