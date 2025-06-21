package com.redhat.sast.ai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_settings")
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

    public JobSettings() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getLlmUrl() {
        return llmUrl;
    }

    public void setLlmUrl(String llmUrl) {
        this.llmUrl = llmUrl;
    }

    public String getLlmModelName() {
        return llmModelName;
    }

    public void setLlmModelName(String llmModelName) {
        this.llmModelName = llmModelName;
    }

    public String getLlmApiKey() {
        return llmApiKey;
    }

    public void setLlmApiKey(String llmApiKey) {
        this.llmApiKey = llmApiKey;
    }

    public String getEmbeddingLlmUrl() {
        return embeddingLlmUrl;
    }

    public void setEmbeddingLlmUrl(String embeddingLlmUrl) {
        this.embeddingLlmUrl = embeddingLlmUrl;
    }

    public String getEmbeddingLlmModelName() {
        return embeddingLlmModelName;
    }

    public void setEmbeddingLlmModelName(String embeddingLlmModelName) {
        this.embeddingLlmModelName = embeddingLlmModelName;
    }

    public String getEmbeddingLlmApiKey() {
        return embeddingLlmApiKey;
    }

    public void setEmbeddingLlmApiKey(String embeddingLlmApiKey) {
        this.embeddingLlmApiKey = embeddingLlmApiKey;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }
} 