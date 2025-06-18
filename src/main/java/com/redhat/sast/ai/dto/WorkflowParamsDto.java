package com.redhat.sast.ai.dto;

public class WorkflowParamsDto {

    private String llmUrl;
    private String llmApiKey;
    private String llmModelName;

    private String embeddingsLlmUrl;
    private String embeddingsLlmApiKey;
    private String embeddingsLlmModelName;

    private boolean runWithCritique;
    private String critiqueLlmUrl;
    private String critiqueLlmApiKey;
    private String critiqueLlmModelName;
    private String useCritiqueAsFinalResults;

    private String srcRepoPath;
    private boolean useKnownFalsePositiveFile;
    private String knownFalsePositiveUrl;
    private String inputReportFilePath;
    private String projectName;
    private String projectVersion;

    public String getLlmUrl() {
        return llmUrl;
    }

    public void setLlmUrl(String llmUrl) {
        this.llmUrl = llmUrl;
    }

    public String getLlmApiKey() {
        return llmApiKey;
    }

    public void setLlmApiKey(String llmApiKey) {
        this.llmApiKey = llmApiKey;
    }

    public String getLlmModelName() {
        return llmModelName;
    }

    public void setLlmModelName(String llmModelName) {
        this.llmModelName = llmModelName;
    }

    public String getEmbeddingsLlmUrl() {
        return embeddingsLlmUrl;
    }

    public void setEmbeddingsLlmUrl(String embeddingsLlmUrl) {
        this.embeddingsLlmUrl = embeddingsLlmUrl;
    }

    public String getEmbeddingsLlmApiKey() {
        return embeddingsLlmApiKey;
    }

    public void setEmbeddingsLlmApiKey(String embeddingsLlmApiKey) {
        this.embeddingsLlmApiKey = embeddingsLlmApiKey;
    }

    public String getEmbeddingsLlmModelName() {
        return embeddingsLlmModelName;
    }

    public void setEmbeddingsLlmModelName(String embeddingsLlmModelName) {
        this.embeddingsLlmModelName = embeddingsLlmModelName;
    }

    public boolean isRunWithCritique() {
        return runWithCritique;
    }

    public void setRunWithCritique(boolean runWithCritique) {
        this.runWithCritique = runWithCritique;
    }

    public String getCritiqueLlmUrl() {
        return critiqueLlmUrl;
    }

    public void setCritiqueLlmUrl(String critiqueLlmUrl) {
        this.critiqueLlmUrl = critiqueLlmUrl;
    }

    public String getCritiqueLlmApiKey() {
        return critiqueLlmApiKey;
    }

    public void setCritiqueLlmApiKey(String critiqueLlmApiKey) {
        this.critiqueLlmApiKey = critiqueLlmApiKey;
    }

    public String getCritiqueLlmModelName() {
        return critiqueLlmModelName;
    }

    public void setCritiqueLlmModelName(String critiqueLlmModelName) {
        this.critiqueLlmModelName = critiqueLlmModelName;
    }

    public String getUseCritiqueAsFinalResults() {
        return useCritiqueAsFinalResults;
    }

    public void setUseCritiqueAsFinalResults(String useCritiqueAsFinalResults) {
        this.useCritiqueAsFinalResults = useCritiqueAsFinalResults;
    }

    public String getSrcRepoPath() {
        return srcRepoPath;
    }

    public void setSrcRepoPath(String srcRepoPath) {
        this.srcRepoPath = srcRepoPath;
    }

    public boolean isUseKnownFalsePositivesFile() {
        return useKnownFalsePositiveFile;
    }

    public void setUseKnownFalsePositivesFile(boolean useKnownFalsePositiveFile) {
        this.useKnownFalsePositiveFile = useKnownFalsePositiveFile;
    }

    public String getKnownFalsePositivesUrl() {
        return knownFalsePositiveUrl;
    }

    public void setKnownFalsePositivesUrl(String knownFalsePositiveUrl) {
        this.knownFalsePositiveUrl = knownFalsePositiveUrl;
    }

    public String getInputReportFilePath() {
        return inputReportFilePath;
    }

    public void setInputReportFilePath(String inputReportFilePath) {
        this.inputReportFilePath = inputReportFilePath;
    }

    public String getProjectName() { 
        return projectName; 
    }
    
    public void setProjectName(String projectName) { 
        this.projectName = projectName; 
    }
    
    public String getProjectVersion() { 
        return projectVersion; 
    }
    
    public void setProjectVersion(String projectVersion) { 
        this.projectVersion = projectVersion; 
    }

}
