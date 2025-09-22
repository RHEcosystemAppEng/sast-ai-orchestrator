# SAST-AI-Orchestrator

[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=RHEcosystemAppEng_sast-ai-orchestrator)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=RHEcosystemAppEng_sast-ai-orchestrator&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=RHEcosystemAppEng_sast-ai-orchestrator)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=RHEcosystemAppEng_sast-ai-orchestrator&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=RHEcosystemAppEng_sast-ai-orchestrator)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=RHEcosystemAppEng_sast-ai-orchestrator&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=RHEcosystemAppEng_sast-ai-orchestrator)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=RHEcosystemAppEng_sast-ai-orchestrator&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=RHEcosystemAppEng_sast-ai-orchestrator)


[![CodeQL](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/codeql.yml/badge.svg)](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/codeql.yml)
[![main branch](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/build-dev-image.yml/badge.svg?branch=main)](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/build-and-publish.yml)

Java Quarkus REST API that manages [SAST-AI-Workflow](https://github.com/RHEcosystemAppEng/sast-ai-workflow) security scanning Tekton pipelines.

## API Endpoints

### Health & Monitoring

#### `GET /api/v1/health`
**Description:** Application health status with dependency checks

**Response:** `200 OK` / `503 Service Unavailable`
```json
{
  "status": "UP|DOWN",
  "timestamp": "2025-01-01T10:00:00",
  "version": "1.0.0",
  "dependencies": {
    "database": "UP",
    "tekton": "UP",
    "google-service-account": "UP - Service account available"
  }
}
```

### Job Management

#### `POST /api/v1/jobs/simple`
**Description:** Create a new security scanning job

**Request Body:**
```json
{
  "packageNvr": "package-name-version-release",
  "inputSourceUrl": "https://example.com/source.zip",
  "submittedBy": "user@example.com", # (optional, defaults to "unknown")
  "useKnownFalsePositiveFile": false # (optional)
}
```

**Response:** `201 Created`
```json
{
  "jobId": 123,
  "packageName": "package-name",
  "packageNvr": "package-name-version-release",
  "sourceCodeUrl": "https://example.com/source.zip",
  "status": "PENDING",
  "createdAt": "2025-01-01T10:00:00",
  "startedAt": null,
  "completedAt": null,
  "cancelledAt": null,
  "tektonUrl": null,
  "batchId": null,
  "projectName": "package-name",
  "projectVersion": "version-release",
  "oshScanId": null,
  "jiraLink": null,
  "hostname": null
}
```

#### `GET /api/v1/jobs`
**Description:** List all jobs with filtering and pagination

**Query Parameters:**
- `packageName` (optional): Filter by package name
- `status` (optional): Filter by status (`PENDING`, `SCHEDULED`, `RUNNING`, `COMPLETED`, `FAILED`, `CANCELLED`)
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size

**Response:** `200 OK`
```json
[
  {
    "jobId": 123,
    "packageName": "package-name",
    "packageNvr": "package-name-version-release",
    "sourceCodeUrl": "https://example.com/source.zip",
    "status": "COMPLETED",
    "createdAt": "2025-01-01T10:00:00",
    "startedAt": "2025-01-01T10:05:00",
    "completedAt": "2025-01-01T10:30:00",
    "tektonUrl": "https://tekton.example.com/pipelineruns/job-123",
    "batchId": null,
    "projectName": "package-name",
    "projectVersion": "version-release",
    "oshScanId": "scan-123",
    "jiraLink": "https://jira.example.com/browse/ISSUE-123",
    "hostname": "worker-node-1"
  }
]
```

#### `GET /api/v1/jobs/{jobId}`
**Description:** Get specific job details

**Response:** `200 OK` - Same structure as job creation response
**Error Responses:**
- `404 Not Found` - Job not found

#### `POST /api/v1/jobs/{jobId}/cancel`
**Description:** Cancel a running job

**Response:** `200 OK`
```json
"Job cancellation requested"
```
**Error Responses:**
- `404 Not Found` - Job not found
- `400 Bad Request` - Job cannot be cancelled (already completed/failed)

### Job Batches

#### `POST /api/v1/job-batches`
**Description:** Submit batch processing jobs from Google Sheets

**Request Body:**
```json
{
  "batchGoogleSheetUrl": "https://docs.google.com/spreadsheets/d/...",
  "submittedBy": "user@example.com", # (optional, defaults to "unknown")
  "useKnownFalsePositiveFile": false # (optional)
}
```

**Response:** `201 Created`
```json
{
  "batchId": 456,
  "batchGoogleSheetUrl": "https://docs.google.com/spreadsheets/d/...",
  "submittedBy": "user@example.com",
  "submittedAt": "2025-01-01T10:00:00",
  "status": "PROCESSING",
  "totalJobs": 10,
  "completedJobs": 0,
  "failedJobs": 0
}
```

#### `GET /api/v1/job-batches`
**Description:** List job batches with pagination

**Query Parameters:**
- `page` (optional, default: 0): Page number  
- `size` (optional, default: 20): Page size

**Response:** `200 OK` - Array of batch objects (same structure as batch creation response)

#### `GET /api/v1/job-batches/{batchId}`
**Description:** Get batch details

**Response:** `200 OK` - Same structure as batch creation response
**Error Responses:**
- `404 Not Found` - Batch not found

#### `POST /api/v1/job-batches/{batchId}:cancel`
**Description:** Cancel a job batch

**Response:** `200 OK`
```json
"Job batch cancellation requested"
```
**Error Responses:**
- `404 Not Found` - Batch not found

### Package Analysis

#### `GET /api/v1/packages`
**Description:** List package vulnerability summaries with pagination

**Query Parameters:**
- `page` (optional, default: 0): Page number
- `size` (optional, default: 50): Page size

**Response:** `200 OK`
```json
[
  {
    "packageName": "example-package",
    "totalAnalyses": 25,
    "lastAnalysisDate": "2025-01-01T10:00:00",
    "completedAnalyses": 20,
    "failedAnalyses": 2,
    "runningAnalyses": 3
  }
]
```

#### `GET /api/v1/packages/{packageName}`
**Description:** Get specific package summary

**Response:** `200 OK` - Same structure as package list item

#### `GET /api/v1/packages/{packageName}/jobs`
**Description:** Get jobs for a specific package

**Query Parameters:**
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size

**Response:** `200 OK` - Array of job objects (same structure as `/api/v1/jobs` response)

### Status Values

**Job Status:**
- `PENDING` - Job created but not yet scheduled
- `SCHEDULED` - Job scheduled for execution
- `RUNNING` - Job currently executing
- `COMPLETED` - Job completed successfully
- `FAILED` - Job failed during execution
- `CANCELLED` - Job was cancelled

**Batch Status:**
- `PROCESSING` - Batch is being processed
- `COMPLETED` - All jobs completed successfully
- `COMPLETED_WITH_ERRORS` - Batch completed but some jobs failed
- `COMPLETED_EMPTY` - Batch completed but contained no valid jobs
- `FAILED` - Batch processing failed
- `CANCELLED` - Batch was cancelled

### Additional Notes

- The multipart form endpoint `POST /api/v1/jobs` exists but is not yet implemented - use `/simple` endpoint instead
- All timestamps are in ISO 8601 format
- All endpoints return JSON responses with appropriate HTTP status codes
- Error responses include descriptive error messages in the response body


## Quick Start

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/RHEcosystemAppEng/sast-ai-orchestrator.git
   cd sast-ai-orchestrator
   ```

2. **Setup PostgreSQL**
   ```bash
   # Using Docker
   docker run --name postgres \
   -e POSTGRES_DB=sast-ai \
   -e POSTGRES_USER=quarkus \
   -e POSTGRES_PASSWORD=quarkus \
   -p 5432:5432 \
   -d postgres:13
   ```

3. **Run the application**
   ```bash
   ./mvnw quarkus:dev
   ```

4. **Access the API**
   ```
   http://localhost:8080/api/v1/health
   ```
   
## Deployment

### Docker Deployment
```bash
# JVM Mode (Fast startup)
docker build -f src/main/docker/Dockerfile.jvm -t sast-ai-orchestrator:jvm .
```

### Kubernetes Deployment
- **Helm Chart**: See `deploy/sast-ai-chart/` for Helm deployment
- **ArgoCD**: See `deploy/argocd/` for GitOps deployment
- **Documentation**: Refer to `deploy/README.md` for detailed instructions

## Configuration

Key configuration options in `application.properties`:

```properties
# Database
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/sast-ai
quarkus.datasource.username=quarkus
quarkus.datasource.password=quarkus

# Workflow Integration  
sast.ai.workflow.namespace=sast-ai
quarkus.kubernetes-client.trust-certs=false
```

---

<div align="center">
  <sub>Built with ❤️ by the Red Hat Ecosystem App Engineering Team</sub>
</div>


