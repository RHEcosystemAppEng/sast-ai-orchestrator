# SAST-AI-Orchestrator

[![Build and Publish Native Image](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/build-and-publish.yml/badge.svg)](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/build-and-publish.yml)
[![Code Quality](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/code-quality.yml/badge.svg)](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/code-quality.yml)
[![CodeQL](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/code-quality.yml/badge.svg?job=codeql)](https://github.com/RHEcosystemAppEng/sast-ai-orchestrator/actions/workflows/code-quality.yml)

> **AI-Powered Static Application Security Testing Orchestration Platform**

A modern, cloud-native backend REST API built with Quarkus that orchestrates and manages [SAST-AI-Workflow](https://github.com/RHEcosystemAppEng/sast-ai-workflow) security scanning jobs across Kubernetes environments.

## Quick Start

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd sast-ai-orchestrator
   ```

2. **Setup PostgreSQL**
   ```bash
   # Using Docker
   docker run --name postgres -e POSTGRES_DB=sast-ai -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -p 5432:5432 -d postgres:13
   ```

3. **Run the application**
   ```bash
   ./mvnw quarkus:dev
   ```

4. **Access the API**
   ```
   http://localhost:8080/api/v1/health
   ```

## API Endpoints

### Health & Monitoring
- `GET /api/v1/health` - Application health status

### Job Management
- `POST /api/v1/jobs/simple` - Create a new security scanning job
- `GET /api/v1/jobs` - List all jobs (with filtering & pagination)  
- `GET /api/v1/jobs/{id}` - Get specific job details
- `POST /api/v1/jobs/{id}:cancel` - Cancel a running job

### Job Batches
- `POST /api/v1/job-batches` - Submit batch processing jobs
- `GET /api/v1/job-batches` - List job batches
- `GET /api/v1/job-batches/{id}` - Get batch details

### Package Analysis
- `GET /api/v1/packages` - Package vulnerability summaries

## Deployment

### Docker Deployment
```bash
# JVM Mode (Fast startup)
docker build -f src/main/docker/Dockerfile.jvm -t sast-ai-orchestrator:jvm .

# Native Mode (Ultra-fast startup, low memory)
docker build -f src/main/docker/Dockerfile.native -t sast-ai-orchestrator:native .
```

### Kubernetes Deployment
```bash
# Deploy to your Kubernetes cluster
kubectl apply -f k8s/
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

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <sub>Built with ❤️ by the Red Hat Ecosystem App Engineering Team</sub>
</div>


