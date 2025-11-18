package com.redhat.sast.api.v1.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.sast.api.testdata.JobTestDataBuilder;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusIntegrationTest
@TestProfile(com.redhat.sast.api.config.TestProfile.class)
@DisplayName("Job Resource Integration Tests")
class JobResourceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobResourceIT.class);

    @Test
    @DisplayName("Should create a single job successfully")
    void shouldCreateJobSuccessfully() {
        JobCreationDto jobRequest = JobTestDataBuilder.treePkgJob();

        Response response = given().contentType(ContentType.JSON)
                .body(jobRequest)
                .when()
                .post("/api/v1/jobs/simple")
                .then()
                .extract()
                .response();

        LOGGER.debug("Response Status: {}", response.statusCode());
        LOGGER.debug("Response Body: {}", response.body().asString());

        Long jobId = response.jsonPath().getLong("jobId");
        assertNotNull(jobId);
    }

    @Test
    @DisplayName("Should retrieve job by ID")
    void shouldRetrieveJobById() {
        JobCreationDto jobRequest = JobTestDataBuilder.unitsJob();
        Long jobId = given().contentType(ContentType.JSON)
                .body(jobRequest)
                .when()
                .post("/api/v1/jobs/simple")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("jobId");

        given().when()
                .get("/api/v1/jobs/{jobId}", jobId)
                .then()
                .statusCode(200)
                .body("jobId", equalTo(jobId.intValue()))
                .body("packageNvr", equalTo("units-2.22-8.el10"))
                .body("status", equalTo("PENDING"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent job")
    void shouldReturn404ForNonExistentJob() {
        given().when().get("/api/v1/jobs/{jobId}", 99999).then().statusCode(404);
    }

    @Test
    @DisplayName("Should retrieve all jobs")
    void shouldRetrieveAllJobs() {
        JobCreationDto job1 = JobTestDataBuilder.xzJob();
        JobCreationDto job2 = JobTestDataBuilder.zlibNgJob();

        given().contentType(ContentType.JSON).body(job1).post("/api/v1/jobs/simple");
        given().contentType(ContentType.JSON).body(job2).post("/api/v1/jobs/simple");

        given().when().get("/api/v1/jobs").then().statusCode(200).body("", hasSize(greaterThan(1)));
    }

    @Test
    @DisplayName("Should filter jobs by package name")
    void shouldFilterJobsByPackageName() {
        JobCreationDto job1 = JobTestDataBuilder.userspaceRcuJob();
        JobCreationDto job2 = JobTestDataBuilder.usbutilsJob();

        given().contentType(ContentType.JSON).body(job1).post("/api/v1/jobs/simple");
        given().contentType(ContentType.JSON).body(job2).post("/api/v1/jobs/simple");

        given().queryParam("packageName", "userspace-rcu")
                .when()
                .get("/api/v1/jobs")
                .then()
                .statusCode(200)
                .body("", hasSize(greaterThan(0)))
                .body("[0].packageNvr", equalTo("userspace-rcu-0.14.0-4.el10"));
    }

    @Test
    @DisplayName("Should filter jobs by status")
    void shouldFilterJobsByStatus() {
        JobCreationDto jobRequest = JobTestDataBuilder.treePkgJob();
        given().contentType(ContentType.JSON).body(jobRequest).post("/api/v1/jobs/simple");

        given().queryParam("status", "PENDING")
                .when()
                .get("/api/v1/jobs")
                .then()
                .statusCode(200)
                .body("", hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("Should handle pagination")
    void shouldHandlePagination() {
        for (int i = 0; i < 3; i++) {
            JobCreationDto job = JobTestDataBuilder.aJob()
                    .withPackageNvr("test-package-" + i + "-1.0.0-1.el10")
                    .build();
            given().contentType(ContentType.JSON).body(job).post("/api/v1/jobs/simple");
        }

        given().queryParam("page", 0)
                .queryParam("size", 2)
                .when()
                .get("/api/v1/jobs")
                .then()
                .statusCode(200)
                .body("", hasSize(2));
    }

    @Test
    @DisplayName("Should cancel a job")
    void shouldCancelJob() {
        JobCreationDto jobRequest = JobTestDataBuilder.treePkgJob();
        Long jobId = given().contentType(ContentType.JSON)
                .body(jobRequest)
                .when()
                .post("/api/v1/jobs/simple")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("jobId");

        given().contentType(ContentType.JSON)
                .when()
                .post("/api/v1/jobs/{jobId}/cancel", jobId)
                .then()
                .statusCode(200);

        given().when()
                .get("/api/v1/jobs/{jobId}", jobId)
                .then()
                .statusCode(200)
                .body("jobId", equalTo(jobId.intValue()));
    }

    @Test
    @DisplayName("Should return 404 when cancelling non-existent job")
    void shouldReturn404WhenCancellingNonExistentJob() {
        given().contentType(ContentType.JSON)
                .when()
                .post("/api/v1/jobs/{jobId}/cancel", 99999)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Should validate required fields")
    void shouldValidateRequiredFields() {
        JobCreationDto invalidJob = new JobCreationDto();
        invalidJob.setInputSourceUrl("https://example.com/report.json");

        given().contentType(ContentType.JSON)
                .body(invalidJob)
                .when()
                .post("/api/v1/jobs/simple")
                .then()
                .statusCode(400);

        JobCreationDto invalidJob2 = new JobCreationDto();
        invalidJob2.setPackageNvr("test-package-1.0.0-1.el10");

        given().contentType(ContentType.JSON)
                .body(invalidJob2)
                .when()
                .post("/api/v1/jobs/simple")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should handle job with known false positives")
    void shouldHandleJobWithKnownFalsePositives() {
        JobCreationDto jobRequest = JobTestDataBuilder.jobWithKnownFalsePositives();

        given().contentType(ContentType.JSON)
                .body(jobRequest)
                .when()
                .post("/api/v1/jobs/simple")
                .then()
                .statusCode(201)
                .body("jobId", notNullValue())
                .body("status", equalTo("PENDING"));
    }

    @Test
    @DisplayName("Should create simple job with aggregate results Google Sheet URL")
    void shouldCreateSimpleJobWithAggregateResultsGSheet() {
        JobCreationDto jobRequest = JobTestDataBuilder.aJob()
                .withPackageNvr("usbutils-017-2.el10")
                .withInputSourceUrl(
                        "https://docs.google.com/spreadsheets/d/1y2V7SSa-FHXuDTRNX5pd3YQ1grHLPt4CvtGBvifBvT8/edit?usp=sharing")
                .withAggregateResultsGSheet(
                        "https://docs.google.com/spreadsheets/d/1B71aAzMlFIZihQOiObXXyfzLV04lqEHriT1bYZMQrtQ/edit?usp=sharing")
                .build();

        given().contentType(ContentType.JSON)
                .body(jobRequest)
                .when()
                .post("/api/v1/jobs/simple")
                .then()
                .statusCode(201)
                .body("jobId", notNullValue())
                .body("status", equalTo("PENDING"));
    }
}
