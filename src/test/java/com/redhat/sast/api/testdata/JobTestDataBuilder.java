package com.redhat.sast.api.testdata;

import com.redhat.sast.api.v1.dto.request.JobCreationDto;

public class JobTestDataBuilder {

    private String packageNvr = "tree-pkg-2.1.0-5.el10";
    private String inputSourceUrl =
            "https://docs.google.com/spreadsheets/d/1Emi-AtukrJ53rVKDsll3ZNdExykkZoX7HlLzZX1tHJ4/edit?usp=sharing";
    private Boolean useKnownFalsePositiveFile = false;
    private String aggregateResultsGSheet = null;

    public static JobTestDataBuilder aJob() {
        return new JobTestDataBuilder();
    }

    public JobTestDataBuilder withPackageNvr(String packageNvr) {
        this.packageNvr = packageNvr;
        return this;
    }

    public JobTestDataBuilder withInputSourceUrl(String inputSourceUrl) {
        this.inputSourceUrl = inputSourceUrl;
        return this;
    }

    public JobTestDataBuilder withUseKnownFalsePositiveFile(Boolean useKnownFalsePositiveFile) {
        this.useKnownFalsePositiveFile = useKnownFalsePositiveFile;
        return this;
    }

    public JobTestDataBuilder withAggregateResultsGSheet(String aggregateResultsGSheet) {
        this.aggregateResultsGSheet = aggregateResultsGSheet;
        return this;
    }

    public JobCreationDto build() {
        JobCreationDto dto = new JobCreationDto(packageNvr, inputSourceUrl);
        dto.setUseKnownFalsePositiveFile(useKnownFalsePositiveFile);
        dto.setAggregateResultsGSheet(aggregateResultsGSheet);
        return dto;
    }

    public static JobCreationDto simpleJob() {
        return aJob().build();
    }

    public static JobCreationDto treePkgJob() {
        return aJob().withPackageNvr("tree-pkg-2.1.0-5.el10")
                .withInputSourceUrl(
                        "https://docs.google.com/spreadsheets/d/1Emi-AtukrJ53rVKDsll3ZNdExykkZoX7HlLzZX1tHJ4/edit?usp=sharing")
                .build();
    }

    public static JobCreationDto unitsJob() {
        return aJob().withPackageNvr("units-2.22-8.el10")
                .withInputSourceUrl(
                        "https://docs.google.com/spreadsheets/d/1aW_bmYVQrtBsUM0LIJd3Y9kTf0lERzly5iAy2vZmlvs/edit?usp=sharing")
                .build();
    }

    public static JobCreationDto userspaceRcuJob() {
        return aJob().withPackageNvr("userspace-rcu-0.14.0-4.el10")
                .withInputSourceUrl(
                        "https://docs.google.com/spreadsheets/d/1QS7Ha4tpZWBvFfCkIInpq6OuiXm3rM62AsI9ywh3xcQ/edit?usp=sharing")
                .build();
    }

    public static JobCreationDto usbutilsJob() {
        return aJob().withPackageNvr("usbutils-017-2.el10")
                .withInputSourceUrl(
                        "https://docs.google.com/spreadsheets/d/1y2V7SSa-FHXuDTRNX5pd3YQ1grHLPt4CvtGBvifBvT8/edit?usp=sharing")
                .build();
    }

    public static JobCreationDto xzJob() {
        return aJob().withPackageNvr("xz-5.4.6-1.el10")
                .withInputSourceUrl(
                        "https://docs.google.com/spreadsheets/d/1_dQ0EoAiUTQUSgxDJqRR5dguTfZn7-8f_SVAxd0-M78/edit?usp=sharing")
                .build();
    }

    public static JobCreationDto zlibNgJob() {
        return aJob().withPackageNvr("zlib-ng-2.1.6-2.el10")
                .withInputSourceUrl(
                        "https://docs.google.com/spreadsheets/d/1WQ-53LYZGGRUwyGcSirKNVBkM6Lr4y2zYVZjX4LjrCQ/edit?usp=sharing")
                .build();
    }

    public static JobCreationDto jobWithKnownFalsePositives() {
        return aJob().withUseKnownFalsePositiveFile(true).build();
    }
}
