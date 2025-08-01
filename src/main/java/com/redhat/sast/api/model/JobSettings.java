package com.redhat.sast.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_settings")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "job")
public class JobSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "job_id")
    private Job job;

    @Column(name = "llm_url")
    private String llmUrl;

    @Column(name = "llm_model_name")
    private String llmModelName;

    @Column(name = "llm_api_key")
    private String llmApiKey;

    @Column(name = "embedding_llm_url")
    private String embeddingLlmUrl;

    @Column(name = "embedding_llm_model_name")
    private String embeddingLlmModelName;

    @Column(name = "embedding_llm_api_key")
    private String embeddingLlmApiKey;

    @Column(name = "secret_name")
    private String secretName;

    @Column(name = "use_known_false_positive_file")
    private Boolean useKnownFalsePositiveFile;
}
