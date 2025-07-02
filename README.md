# SAST-AI-Orchestrator

[![CodeQL](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/codeql.yml/badge.svg)](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/codeql.yml)
[![Build and Publish JVM Image](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/build-and-publish.yml/badge.svg?branch=main)](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/build-and-publish.yml)

> **AI-Powered SAST Orchestration Platform**

Java Quarkus REST API that manages [SAST-AI-Workflow](https://github.com/RHEcosystemAppEng/sast-ai-workflow) security scanning Tekton pipelines.

## API Endpoints

#### Health & Monitoring
- `GET /api/v1/health` - Application health status

#### Job Management
- `POST /api/v1/jobs/simple` - Create a new security scanning job
- `GET /api/v1/jobs` - List all jobs (with filtering & pagination)  
- `GET /api/v1/jobs/{id}` - Get specific job details
- `POST /api/v1/jobs/{id}:cancel` - Cancel a running job

#### Job Batches
- `POST /api/v1/job-batches` - Submit batch processing jobs
- `GET /api/v1/job-batches` - List job batches
- `GET /api/v1/job-batches/{id}` - Get batch details

#### Package Analysis
- `GET /api/v1/packages` - Package vulnerability summaries


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


