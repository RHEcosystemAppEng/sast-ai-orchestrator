# Grafana Dashboards for SAST-AI MLOps Metrics

This directory contains Grafana dashboard configurations and setup scripts for monitoring MLOps batch metrics and job performance in the SAST-AI Orchestrator.

## Overview

The Grafana setup provides real-time visualization of:
- **Batch-level metrics**: Aggregated performance across entire batches
- **Job-level metrics**: Individual job performance with detailed confusion matrices
- **Version comparisons**: Track improvements across different prompt/data versions
- **Timeline views**: Monitor job completion and batch progress

## Directory Structure

```
grafana/
├── dashboards/              # Dashboard JSON configurations
│   ├── mlops-batch-metrics.json   # Batch-level metrics overview
│   └── mlops-job-details.json     # Job-level metrics with filtering
├── datasources/             # Datasource configurations
│   └── postgresql.yml             # PostgreSQL datasource config
├── provisioning/            # Grafana provisioning configs
│   └── dashboards.yml             # Dashboard provisioning settings
├── sample-data.sql          # Sample data for testing
├── setup-datasource.sh      # Script to configure datasource
├── import-dashboards.sh     # Script to import dashboards
└── README.md               # This file
```

## Prerequisites

- PostgreSQL database with MLOps tables (`mlops_batch`, `mlops_job`, `mlops_job_metrics`)
- Grafana 9.0+ (included in Docker Compose setup)
- Database migrations applied (V004 and V005)

## Quick Start

### Automated Setup (Recommended)

Run the automated setup script that handles everything:

```bash
./scripts/setup_grafana.sh
```

This script will:
- ✓ Check prerequisites (podman, PostgreSQL)
- ✓ Apply all database migrations
- ✓ Start Grafana container
- ✓ Configure PostgreSQL datasource with correct UID
- ✓ Import all dashboards
- ✓ Verify the setup

**Access Grafana**: http://localhost:3000
- Username: `admin`
- Password: `admin`

### Manual Setup

If you prefer to set up manually:

1. **Start Grafana**:
   ```bash
   podman run -d \
     --name sast-ai-grafana \
     -e GF_SECURITY_ADMIN_USER=admin \
     -e GF_SECURITY_ADMIN_PASSWORD=admin \
     -p 3000:3000 \
     grafana/grafana:9.5.0
   ```

2. **Apply database migrations** (to your PostgreSQL):
   ```bash
   psql -h localhost -p 5432 -U quarkus -d sast-ai -f src/main/resources/db/migration/V006__create_mlops_tables.sql
   psql -h localhost -p 5432 -U quarkus -d sast-ai -f src/main/resources/db/migration/V007__create_mlops_issue_details.sql
   psql -h localhost -p 5432 -U quarkus -d sast-ai -f src/main/resources/db/migration/V005__create_mlops_batch_metrics_view.sql
   ```

3. **Configure Grafana datasource**:
   ```bash
   ./grafana/setup-datasource.sh
   ```

4. **Import dashboards**:
   ```bash
   ./grafana/import-dashboards.sh
   ```

5. **Access Grafana**: http://localhost:3000
   - Username: `admin`
   - Password: `admin`

### Option 2: Connect to Cluster Database

1. **Setup port forwarding to cluster PostgreSQL**:
   ```bash
   oc port-forward svc/sast-ai-orchestrator-postgresql 5433:5432 -n sast-ai-mlops
   ```

2. **Apply database migrations** (if not already applied):
   ```bash
   PGPASSWORD=quarkus psql -h localhost -p 5433 -U quarkus -d sast-ai \
     -f src/main/resources/db/migration/V005__create_mlops_batch_metrics_view.sql
   ```

3. **Start Grafana** (if not running):
   ```bash
   docker-compose up -d grafana
   ```

4. **Configure production datasource**:
   ```bash
   # Edit the datasource URL in Grafana UI or update postgresql.yml:
   # url: host.docker.internal:5433
   ./grafana/setup-datasource.sh
   ```

5. **Import dashboards**:
   ```bash
   ./grafana/import-dashboards.sh
   ```

## Database Schema

### MLOps Tables

#### `mlops_batch`
Tracks batch-level information and DVC version control:
- `testing_data_nvrs_version`: DVC version of test data
- `prompts_version`: Version of prompts used
- `known_non_issues_version`: Version of known false positives
- `container_image`: Container image used for processing

#### `mlops_job`
Individual job execution details:
- `package_nvr`, `package_name`: Package information
- `status`: Job status (PENDING, RUNNING, COMPLETED, FAILED, CANCELLED)
- `created_at`, `started_at`, `completed_at`: Timestamps

#### `mlops_job_metrics`
Performance metrics for each job:
- `accuracy`, `precision`, `recall`, `f1_score`: ML metrics
- `cm_tp`, `cm_fp`, `cm_tn`, `cm_fn`: Confusion matrix components

#### `mlops_batch_metrics_view` (Aggregated View)
Automatically aggregates metrics by batch:
- `avg_accuracy`, `avg_precision`, etc.: Simple averages
- `batch_accuracy`, `batch_precision`, etc.: Weighted by confusion matrix totals
- `total_tp`, `total_fp`, `total_tn`, `total_fn`: Summed confusion matrices

## Dashboards

### 1. MLOps Batch Metrics Overview

**URL**: `/d/mlops-batch-metrics/mlops-batch-metrics-overview`

**Features**:
- **Gauge Panels**: Latest batch accuracy, precision, recall, F1 score
- **Time Series**: Performance trends over time
- **Confusion Matrix Table**: Recent batches with detailed metrics
- **Batch Comparison**: Side-by-side comparison of batches
- **Version Performance**: Chart comparing different prompt versions

**Key Queries**:
```sql
-- Latest batch metrics
SELECT batch_accuracy FROM mlops_batch_metrics_view
ORDER BY submitted_at DESC LIMIT 1;

-- Performance over time
SELECT
  submitted_at AS time,
  batch_accuracy AS "Accuracy",
  batch_precision AS "Precision",
  batch_recall AS "Recall",
  batch_f1_score AS "F1 Score"
FROM mlops_batch_metrics_view
WHERE $__timeFilter(submitted_at)
ORDER BY submitted_at;
```

### 2. MLOps Job Details

**URL**: `/d/mlops-job-details/mlops-job-details`

**Features**:
- **Batch Selector**: Dropdown to filter by specific batch
- **Job Metrics Distribution**: Bar chart showing metrics across jobs
- **Detailed Job Table**: Individual job metrics with confusion matrix
- **Job Completion Timeline**: Track job completion over time

**Variables**:
- `$batch_id`: Selected batch ID for filtering

**Key Queries**:
```sql
-- Detailed job metrics for selected batch
SELECT
  j.id AS "Job ID",
  j.package_name AS "Package Name",
  jm.accuracy AS "Accuracy",
  jm.precision AS "Precision",
  jm.recall AS "Recall",
  jm.f1_score AS "F1 Score",
  jm.cm_tp AS "TP",
  jm.cm_fp AS "FP"
FROM mlops_job j
LEFT JOIN mlops_job_metrics jm ON j.id = jm.mlops_job_id
WHERE j.mlops_batch_id = ${batch_id}
ORDER BY j.created_at DESC;
```

## Configuration

### Datasource Configuration

The datasource configuration supports multiple environments:

**Local Docker**:
```yaml
url: postgres:5432
database: sast-ai
user: quarkus
password: quarkus  # Via secureJsonData
```

**Port-Forward to Cluster**:
```yaml
url: host.docker.internal:5433
database: sast-ai
user: quarkus
password: quarkus  # Retrieved from cluster secret
```

**Kubernetes/OpenShift**:
```yaml
url: sast-ai-orchestrator-postgresql.sast-ai-mlops.svc.cluster.local:5432
database: sast-ai
user: quarkus
password: ${POSTGRES_PASSWORD}  # From secret
```

### Environment Variables

- `POSTGRES_PASSWORD`: Database password (defaults to 'quarkus')
- `GF_SECURITY_ADMIN_PASSWORD`: Grafana admin password (defaults to 'admin')

## Maintenance

### Updating Dashboards

1. **Edit in Grafana UI**: Make changes through the web interface
2. **Export updated version**:
   ```bash
   curl -u admin:admin http://localhost:3000/api/dashboards/uid/mlops-batch-metrics > grafana/dashboards/mlops-batch-metrics.json
   ```
3. **Commit changes** to version control

### Adding New Panels

1. Add panel in Grafana UI
2. Configure query using `mlops_batch_metrics_view` or direct tables
3. Export and commit the updated dashboard JSON

### Troubleshooting

**No data showing**:
```bash
# Check datasource connection
curl -u admin:admin http://localhost:3000/api/datasources

# Test database query
PGPASSWORD=quarkus psql -h localhost -p 5432 -U quarkus -d sast-ai \
  -c "SELECT COUNT(*) FROM mlops_batch_metrics_view;"
```

**Datasource authentication failing**:
```bash
# Reconfigure datasource
./grafana/setup-datasource.sh
```

**Dashboards not loading**:
```bash
# Reimport dashboards
./grafana/import-dashboards.sh
```

## Metrics Interpretation

### Accuracy Metrics
- **Accuracy**: (TP + TN) / (TP + FP + TN + FN)
- **Precision**: TP / (TP + FP) - "Of predicted issues, how many are real?"
- **Recall**: TP / (TP + FN) - "Of real issues, how many did we catch?"
- **F1 Score**: 2 × (Precision × Recall) / (Precision + Recall)

### Average vs Batch Metrics
- **Average Metrics** (`avg_*`): Simple average across all jobs (treats each job equally)
- **Batch Metrics** (`batch_*`): Weighted by actual predictions (summed confusion matrices)

**Example**:
- Job 1: 90% accuracy on 1000 samples
- Job 2: 50% accuracy on 100 samples
- **Average**: 70% (misleading)
- **Batch**: 87.27% (more accurate - weighted by samples)

## Production Deployment

For production deployment in Kubernetes/OpenShift:

1. **Deploy Grafana as a separate service** (not in docker-compose)
2. **Use Kubernetes secrets** for database credentials
3. **Configure datasource** to use internal service name
4. **Set up persistent volumes** for Grafana data
5. **Configure RBAC** for appropriate access control

See `deploy/` directory for Helm chart integration (if available).

## Sample Data

The `sample-data.sql` file provides test data:
- 3 batches with different performance levels
- 23 jobs total across batches
- Realistic metrics and confusion matrices

Use for testing dashboard functionality before connecting to production data.

## Support

For issues or questions:
- Check the main project README
- Review database migration status
- Verify network connectivity to PostgreSQL
- Check Grafana logs: `docker logs sast-ai-grafana`

## License

Same as parent project (see root LICENSE file).