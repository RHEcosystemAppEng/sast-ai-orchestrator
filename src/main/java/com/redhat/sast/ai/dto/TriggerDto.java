package com.redhat.sast.ai.dto;

public class TriggerDto {
    public String packageName;
    public String packageNvr;
    public String oshScanId;
    public String sourceCodeUrl;
    public String inputReportFilePath;
    public String jiraLink;
    public String hostname;
    public String projectName;
    public String projectVersion;
    public String falsePositivesUrl;
    public Settings workflowSettings;

    public TriggerDto() {
    }

    public TriggerDto(String packageName, String packageNvr, String oshScanId, String sourceCodeUrl, String inputReportFilePath, String jiraLink, String hostname, Settings workflowSettings) {
        this.packageName = packageName;
        this.packageNvr = packageNvr;
        this.oshScanId = oshScanId;
        this.sourceCodeUrl = sourceCodeUrl;
        this.inputReportFilePath = inputReportFilePath;
        this.jiraLink = jiraLink;
        this.hostname = hostname;
        this.workflowSettings = workflowSettings;
    }

    @Override
    public String toString() {
        return "TriggerDto{" +
                "packageName='" + packageName + '\'' +
                ", packageNvr='" + packageNvr + '\'' +
                ", oshScanId=" + oshScanId +
                ", sourceCodeUrl='" + sourceCodeUrl + '\'' +
                ", inputReportFilePath='" + inputReportFilePath + '\'' +
                ", jiraLink='" + jiraLink + '\'' +
                ", hostname='" + hostname + '\'' +
                ", workflowSettings=" + workflowSettings +
                '}';
    }

    public static class Settings {
        public String llmUrl;
        public String llmModelName;
        public String llmAPIKey;

        public String embeddingsLlmUrl;
        public String embeddingsLlmModelName;
        public String embeddingsLlmAPIKey;

        public Settings() {
        }

        public Settings(String llmUrl, String llmModelName, String llmAPIKey, String embeddingsLlmUrl, String embeddingsLlmModelName, String embeddingsLlmAPIKey) {
            this.llmUrl = llmUrl;
            this.llmModelName = llmModelName;
            this.llmAPIKey = llmAPIKey;
            this.embeddingsLlmUrl = embeddingsLlmUrl;
            this.embeddingsLlmModelName = embeddingsLlmModelName;
            this.embeddingsLlmAPIKey = embeddingsLlmAPIKey;
        }

        @Override
        public String toString() {
            return "Settings{" +
                    "llmUrl='" + llmUrl + '\'' +
                    ", llmModelName='" + llmModelName + '\'' +
                    ", llmAPIKey='" + llmAPIKey + '\'' +
                    ", embeddingsLlmUrl='" + embeddingsLlmUrl + '\'' +
                    ", embeddingsLlmModelName='" + embeddingsLlmModelName + '\'' +
                    ", embeddingsLlmAPIKey='" + embeddingsLlmAPIKey + '\'' +
                    '}';
        }
    }
}
