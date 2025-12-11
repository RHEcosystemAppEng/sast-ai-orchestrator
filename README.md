# SAST-AI-Orchestrator

[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=RHEcosystemAppEng_sast-ai-orchestrator)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=RHEcosystemAppEng_sast-ai-orchestrator&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=RHEcosystemAppEng_sast-ai-orchestrator)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=RHEcosystemAppEng_sast-ai-orchestrator&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=RHEcosystemAppEng_sast-ai-orchestrator)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=RHEcosystemAppEng_sast-ai-orchestrator&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=RHEcosystemAppEng_sast-ai-orchestrator)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=RHEcosystemAppEng_sast-ai-orchestrator&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=RHEcosystemAppEng_sast-ai-orchestrator)


[![CodeQL](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/codeql.yml/badge.svg)](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/codeql.yml)
[![Build Dev Image](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/build-dev-image.yml/badge.svg)](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/build-dev-image.yml)

Orchestrator REST API that manages [SAST-AI-Workflow](https://github.com/RHEcosystemAppEng/sast-ai-workflow) security scanning Red Hat OpenShift pipelines.

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
    "tektonUrl": "https://console-openshift-console.apps.appeng.clusters.se-apps.redhat.com/k8s/ns/sast-ai/tekton.dev~v1~PipelineRun/sast-ai-workflow-pipeline-123/",
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

### MLOps Batch

MLOps batch endpoints enable automated testing of multiple NVRs (Name-Version-Release) fetched from DVC (Data Version Control). These endpoints are independent from regular job batches and designed specifically for ML operations workflows.

#### `POST /api/v1/mlops-batch`
**Description:** Submit MLOps batch for processing. Fetches NVR list from DVC and creates individual jobs for each NVR with specified MLOps parameters.

**Request Body:**
```json
{
  "testingDataNvrsVersion": "1.0",
  "promptsVersion": "1.0",
  "knownNonIssuesVersion": "1.0",
  "sastAiImage": "quay.io/ecosystem-appeng/sast-ai-workflow:latest",
  "submittedBy": "user@example.com"
}
```

**Fields:**
- `testingDataNvrsVersion` (required): DVC version tag for fetching test NVRs
- `promptsVersion` (required): DVC version for prompts configuration
- `knownNonIssuesVersion` (required): DVC version for known non-issues data
- `sastAiImage` (required): Container image for SAST AI workflow
- `submittedBy` (optional): User or system that submitted the batch

**Response:** `201 Created`
```json
{
  "batchId": 123,
  "testingDataNvrsVersion": "1.0",
  "promptsVersion": "1.0",
  "knownNonIssuesVersion": "1.0",
  "sastAiImage": "quay.io/ecosystem-appeng/sast-ai-workflow:latest",
  "submittedBy": "user@example.com",
  "submittedAt": "2025-01-01T10:00:00",
  "status": "PROCESSING",
  "totalJobs": 0,
  "completedJobs": 0,
  "failedJobs": 0,
  "lastUpdatedAt": "2025-01-01T10:00:00"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid request body or DVC fetch failure

#### `GET /api/v1/mlops-batch`
**Description:** List all MLOps batches with pagination

**Query Parameters:**
- `page` (optional, default: 0): Page number  
- `size` (optional, default: 20): Page size

**Response:** `200 OK` - Array of MLOps batch objects (same structure as batch creation response)

#### `GET /api/v1/mlops-batch/{batchId}`
**Description:** Get specific MLOps batch details

**Response:** `200 OK` - Same structure as batch creation response

**Error Responses:**
- `404 Not Found` - MLOps batch not found

#### `GET /api/v1/mlops-batch/{batchId}/detailed`
**Description:** Get detailed MLOps batch information including all jobs and their metrics

**Response:** `200 OK`
```json
{
  "batchId": 123,
  "testingDataNvrsVersion": "1.0",
  "promptsVersion": "1.0",
  "knownNonIssuesVersion": "1.0",
  "sastAiImage": "quay.io/ecosystem-appeng/sast-ai-workflow:latest",
  "submittedBy": "user@example.com",
  "submittedAt": "2025-01-01T10:00:00",
  "status": "COMPLETED_WITH_ERRORS",
  "totalJobs": 5,
  "completedJobs": 3,
  "failedJobs": 2,
  "lastUpdatedAt": "2025-01-01T11:00:00",
  "jobs": [
    {
      "jobId": 456,
      "packageNvr": "acl-2.3.2-1.el10",
      "packageName": "acl",
      "projectName": "acl",
      "projectVersion": "2.3.2-1",
      "packageSourceCodeUrl": "https://download.devel.redhat.com/brewroot/vol/rhel-10/packages/acl/2.3.2/1.el10/src/acl-2.3.2-1.el10.src.rpm",
      "knownFalsePositivesUrl": "https://gitlab.cee.redhat.com/osh/known-false-positives/-/raw/master/acl/ignore.err",
      "status": "COMPLETED",
      "createdAt": "2025-01-01T10:00:00",
      "startedAt": "2025-01-01T10:01:00",
      "completedAt": "2025-01-01T10:30:00",
      "tektonUrl": "https://console-openshift-console.apps.appeng.clusters.se-apps.redhat.com/k8s/ns/sast-ai/tekton.dev~v1~PipelineRun/sast-ai-workflow-pipeline-456/",
      "metrics": {
        "accuracy": 0.95,
        "precision": 0.87,
        "recall": 0.92,
        "f1Score": 0.89,
        "confusionMatrix": {
          "tp": 10,
          "fp": 2,
          "tn": 8,
          "fn": 1
        }
      }
    }
  ]
}
```

**Error Responses:**
- `404 Not Found` - MLOps batch not found

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

### Environment Strategy

The project supports two deployment environments:

- **Development** (`sast-ai-dev` namespace): 
  - Uses `latest` container images
  - Updated automatically on every main branch push
  - Debug logging and relaxed resource limits
  
- **Production** (`sast-ai-prod` namespace):
  - Uses release-tagged container images (e.g., `v1.0.1`)
  - Updated only on GitHub releases
  - Production-grade resource allocation and logging

### Quick Deployment

```bash
# Development environment
cd deploy
make deploy-dev

# Production environment  
cd deploy
make deploy-prod

# Check deployment status
make status
```

### Container Images

- **Development**: `quay.io/ecosystem-appeng/sast-ai-orchestrator:latest`
- **Production**: `quay.io/ecosystem-appeng/sast-ai-orchestrator:v1.0.x`

### Manual Dev Image Builds

The **Build Dev Image** workflow supports manual triggering via GitHub Actions UI, allowing developers to build and push dev images from any branch.

**To trigger a manual build:**

1. Navigate to **Actions** → **Build Dev Image** → **Run workflow**
2. Select the branch to build from
3. Configure options:
   - **Image tag**: Custom tag (defaults to branch name, or `latest` for main)
   - **Push image**: Toggle whether to push to registry (default: true)

**Use cases:**
- Test feature branch images before merging
- Build images with custom tags for specific testing scenarios
- Build without pushing to verify the build process

**Notes:**
- Manual dispatches require the `dev-image-builds` environment approval
- The `latest` tag is reserved for main branch builds and cannot be used for manual dispatches
- Branch names with `/` are converted to `-` in the tag (e.g., `feature/foo` → `feature-foo`)

### Docker Deployment
```bash
# Development (latest)
docker run -p 8080:8080 quay.io/ecosystem-appeng/sast-ai-orchestrator:latest

# Production (specific version)  
docker run -p 8080:8080 quay.io/ecosystem-appeng/sast-ai-orchestrator:v1.0.1
```

### Kubernetes Deployment
- **Helm Chart**: See `deploy/sast-ai-chart/` for Helm deployment
- **ArgoCD**: See `deploy/argocd/` for GitOps deployment  
- **Documentation**: Use `make help` in the `deploy/` directory for available commands

### Environment-Specific Access

After deployment, access the applications via OpenShift routes:

```bash
# Get the route URL for production
kubectl get route sast-ai-orchestrator-prod -n sast-ai-prod

# Get the route URL for development
kubectl get route sast-ai-orchestrator-dev -n sast-ai-dev

# Access the API directly via route
curl https://<route-hostname>/api/v1/health
```

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


