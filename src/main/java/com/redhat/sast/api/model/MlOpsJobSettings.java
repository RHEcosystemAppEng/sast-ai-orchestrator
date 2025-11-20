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

    @Column(name = "embedding_llm_url")
    private String embeddingLlmUrl;

    @Column(name = "embedding_llm_model_name")
    private String embeddingLlmModelName;

    @Column(name = "secret_name")
    private String secretName;

    @Column(name = "use_known_false_positive_file")
    private Boolean useKnownFalsePositiveFile;
}
