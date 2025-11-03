package com.redhat.sast.api.v1.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.v1.dto.request.MlOpsJobBatchSubmissionDto;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusIntegrationTest
@TestProfile(com.redhat.sast.api.config.TestProfile.class)
@DisplayName("MLOps Job Batch Resource Integration Tests")
class MlOpsJobBatchResourceIT {

    @Test
    @DisplayName("Should submit an MLOps job batch successfully")
    void shouldSubmitMlOpsJobBatchSuccessfully() {
        MlOpsJobBatchSubmissionDto batchRequest = new MlOpsJobBatchSubmissionDto(
                "mlops-test-user-1",
                true,
                "v1.0.0-nvr",
                "v1.0.0-fp",
                "v1.0.0-prompts",
                "v2.1.0");

        Response response = given().contentType(ContentType.JSON)
                .body(batchRequest)
                .when()
                .post("/api/v1/mlops-batches")
                .then()
                .statusCode(201)
                .body("batchId", notNullValue())
                .body("submittedBy", equalTo("mlops-test-user-1"))
                .body("status", notNullValue())
                .body("submittedAt", notNullValue())
                .extract()
                .response();

        Long batchId = response.jsonPath().getLong("batchId");
        assertNotNull(batchId);
    }

    @Test
    @DisplayName("Should retrieve MLOps job batch by ID")
    void shouldRetrieveMlOpsJobBatchById() {
        MlOpsJobBatchSubmissionDto batchRequest = new MlOpsJobBatchSubmissionDto(
                "mlops-test-user-2",
                true,
                "v2.0.0-nvr",
                "v2.0.0-fp",
                "v2.0.0-prompts",
                "v2.2.0");

        Long batchId = given().contentType(ContentType.JSON)
                .body(batchRequest)
                .when()
                .post("/api/v1/mlops-batches")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("batchId");

        given().when()
                .get("/api/v1/mlops-batches/{batchId}", batchId)
                .then()
                .statusCode(200)
                .body("batchId", equalTo(batchId.intValue()))
                .body("submittedBy", equalTo("mlops-test-user-2"))
                .body("status", notNullValue());
    }

    @Test
    @DisplayName("Should return 404 for non-existent MLOps batch")
    void shouldReturn404ForNonExistentMlOpsBatch() {
        given().when().get("/api/v1/mlops-batches/{batchId}", 99999).then().statusCode(404);
    }

    @Test
    @DisplayName("Should retrieve all MLOps job batches")
    void shouldRetrieveAllMlOpsJobBatches() {
        MlOpsJobBatchSubmissionDto batch1 = new MlOpsJobBatchSubmissionDto(
                "mlops-user-1",
                true,
                "v3.0.0-nvr",
                "v3.0.0-fp",
                "v3.0.0-prompts",
                "v3.0.0");

        MlOpsJobBatchSubmissionDto batch2 = new MlOpsJobBatchSubmissionDto(
                "mlops-user-2",
                true,
                "v3.1.0-nvr",
                "v3.1.0-fp",
                "v3.1.0-prompts",
                "v3.1.0");

        given().contentType(ContentType.JSON).body(batch1).post("/api/v1/mlops-batches");
        given().contentType(ContentType.JSON).body(batch2).post("/api/v1/mlops-batches");

        given().when().get("/api/v1/mlops-batches").then().statusCode(200).body("", hasSize(greaterThan(1)));
    }

    @Test
    @DisplayName("Should handle pagination for MLOps job batches")
    void shouldHandlePaginationForMlOpsJobBatches() {
        for (int i = 0; i < 3; i++) {
            MlOpsJobBatchSubmissionDto batch = new MlOpsJobBatchSubmissionDto(
                    "mlops-pagination-user-" + i,
                    true,
                    "v4." + i + ".0-nvr",
                    "v4." + i + ".0-fp",
                    "v4." + i + ".0-prompts",
                    "v4." + i + ".0");
            given().contentType(ContentType.JSON).body(batch).post("/api/v1/mlops-batches");
        }

        given().queryParam("page", 0)
                .queryParam("size", 2)
                .when()
                .get("/api/v1/mlops-batches")
                .then()
                .statusCode(200)
                .body("", hasSize(2));
    }

    @Test
    @DisplayName("Should reject MLOps batch submission with missing required fields")
    void shouldRejectMlOpsBatchSubmissionWithMissingFields() {
        String invalidJson = """
                {
                  "submittedBy": "test-user"
                }
                """;

        given().contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/api/v1/mlops-batches")
                .then()
                .statusCode(400); // Should fail validation due to missing required fields
    }

    @Test
    @DisplayName("Should accept MLOps batch submission with minimal fields")
    void shouldAcceptMlOpsBatchSubmissionWithMinimalFields() {
        MlOpsJobBatchSubmissionDto batchRequest = new MlOpsJobBatchSubmissionDto(
                "test-user",
                true,
                "v5.0.0-nvr",
                "v5.0.0-fp",
                "v5.0.0-prompts",
                "v5.0.0");

        given().contentType(ContentType.JSON)
                .body(batchRequest)
                .when()
                .post("/api/v1/mlops-batches")
                .then()
                .statusCode(201);
    }
}

