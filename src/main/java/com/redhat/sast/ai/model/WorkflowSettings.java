package com.redhat.sast.ai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workflowSettings")
public class WorkflowSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "workflow_id")
    public Workflow workflow;

    @Column(name = "llm_url")
    private String llmUrl;

    @Column(name = "llm_model_name")
    private String llmModelName;

    @Column(name = "embedding_llm_url")
    private String embeddingLlmUrl;

    @Column(name = "embedding_llm_model_name")
    private String embeddingLlmModelName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
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
}
