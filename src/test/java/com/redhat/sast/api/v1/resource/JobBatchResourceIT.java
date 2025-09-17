package com.redhat.sast.api.v1.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.testdata.JobBatchTestDataBuilder;
import com.redhat.sast.api.v1.dto.request.JobBatchSubmissionDto;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusIntegrationTest
@TestProfile(com.redhat.sast.api.config.TestProfile.class)
@DisplayName("Job Batch Resource Integration Tests")
class JobBatchResourceIT {

    @Test
    @DisplayName("Should submit a job batch successfully")
    void shouldSubmitJobBatchSuccessfully() {
        JobBatchSubmissionDto batchRequest = JobBatchTestDataBuilder.firstBatchDataset();

        Response response = given().contentType(ContentType.JSON)
                .body(batchRequest)
                .when()
                .post("/api/v1/job-batches")
                .then()
                .statusCode(201)
                .body("batchId", notNullValue())
                .body("submittedBy", equalTo("integration-test-user-1"))
                .body(
                        "batchGoogleSheetUrl",
                        equalTo(
                                "https://docs.google.com/spreadsheets/d/1wrIcIhC7F9uVf8fm0IlvGTO-dSV9t_maLx36OoRI7S0/edit?usp=sharing"))
                .body("status", notNullValue())
                .body("submittedAt", notNullValue())
                .extract()
                .response();

        Long batchId = response.jsonPath().getLong("batchId");
        assertNotNull(batchId);
    }

    @Test
    @DisplayName("Should retrieve job batch by ID")
    void shouldRetrieveJobBatchById() {
        JobBatchSubmissionDto batchRequest = JobBatchTestDataBuilder.secondBatchDataset();
        Long batchId = given().contentType(ContentType.JSON)
                .body(batchRequest)
                .when()
                .post("/api/v1/job-batches")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("batchId");

        given().when()
                .get("/api/v1/job-batches/{batchId}", batchId)
                .then()
                .statusCode(200)
                .body("batchId", equalTo(batchId.intValue()))
                .body("submittedBy", equalTo("integration-test-user-2"))
                .body(
                        "batchGoogleSheetUrl",
                        equalTo(
                                "https://docs.google.com/spreadsheets/d/1GcJg8aHfpEGxrPbb2gYD-CadwoxAWEB91Er5q0RpOuE/edit?usp=sharing"))
                .body("status", notNullValue());
    }

    @Test
    @DisplayName("Should return 404 for non-existent batch")
    void shouldReturn404ForNonExistentBatch() {
        given().when().get("/api/v1/job-batches/{batchId}", 99999).then().statusCode(404);
    }

    @Test
    @DisplayName("Should retrieve all job batches")
    void shouldRetrieveAllJobBatches() {
        JobBatchSubmissionDto batch1 = JobBatchTestDataBuilder.batchForUser("test-user-1");
        JobBatchSubmissionDto batch2 = JobBatchTestDataBuilder.batchForUser("test-user-2");

        given().contentType(ContentType.JSON).body(batch1).post("/api/v1/job-batches");
        given().contentType(ContentType.JSON).body(batch2).post("/api/v1/job-batches");

        given().when().get("/api/v1/job-batches").then().statusCode(200).body("", hasSize(greaterThan(1)));
    }

    @Test
    @DisplayName("Should handle pagination for job batches")
    void shouldHandlePaginationForJobBatches() {
        for (int i = 0; i < 3; i++) {
            JobBatchSubmissionDto batch = JobBatchTestDataBuilder.batchForUser("pagination-test-user-" + i);
            given().contentType(ContentType.JSON).body(batch).post("/api/v1/job-batches");
        }

        given().queryParam("page", 0)
                .queryParam("size", 2)
                .when()
                .get("/api/v1/job-batches")
                .then()
                .statusCode(200)
                .body("", hasSize(2));
    }

    @Test
    @DisplayName("Should validate required fields for batch submission")
    void shouldValidateRequiredFieldsForBatchSubmission() {
        JobBatchSubmissionDto invalidBatch = new JobBatchSubmissionDto();
        invalidBatch.setSubmittedBy("test-user");

        given().contentType(ContentType.JSON)
                .body(invalidBatch)
                .when()
                .post("/api/v1/job-batches")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should handle batch with known false positives")
    void shouldHandleBatchWithKnownFalsePositives() {
        JobBatchSubmissionDto batchRequest = JobBatchTestDataBuilder.batchWithKnownFalsePositives();

        given().contentType(ContentType.JSON)
                .body(batchRequest)
                .when()
                .post("/api/v1/job-batches")
                .then()
                .statusCode(201)
                .body("batchId", notNullValue())
                .body("status", notNullValue())
                .body("submittedBy", equalTo("integration-test-fp-user"));
    }

    @Test
    @DisplayName("Should create individual jobs within a batch")
    void shouldCreateIndividualJobsWithinBatch() {
        JobBatchSubmissionDto batchRequest = JobBatchTestDataBuilder.firstBatchDataset();

        Response response = given().contentType(ContentType.JSON)
                .body(batchRequest)
                .when()
                .post("/api/v1/job-batches")
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long batchId = response.jsonPath().getLong("batchId");
        given().when().get("/api/v1/job-batches/{batchId}", batchId).then().statusCode(200);
    }

    @Test
    @DisplayName("Should handle batch submission with valid Google Sheets URL")
    void shouldHandleBatchSubmissionWithValidGoogleSheetsUrl() {
        JobBatchSubmissionDto batchRequest = JobBatchTestDataBuilder.aBatch()
                .withBatchGoogleSheetUrl(
                        "https://docs.google.com/spreadsheets/d/1wrIcIhC7F9uVf8fm0IlvGTO-dSV9t_maLx36OoRI7S0/edit?usp=sharing")
                .withSubmittedBy("valid-url-test-user")
                .build();

        given().contentType(ContentType.JSON)
                .body(batchRequest)
                .when()
                .post("/api/v1/job-batches")
                .then()
                .statusCode(201)
                .body(
                        "batchGoogleSheetUrl",
                        equalTo(
                                "https://docs.google.com/spreadsheets/d/1wrIcIhC7F9uVf8fm0IlvGTO-dSV9t_maLx36OoRI7S0/edit?usp=sharing"));
    }
}
