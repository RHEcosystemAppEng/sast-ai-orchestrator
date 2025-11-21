# Grafana Dashboards for OpenShift

This directory contains manifests and scripts to deploy Grafana dashboards to OpenShift using the Grafana Operator.

## Prerequisites

- OpenShift cluster with Grafana Operator installed
- `oc` CLI tool installed and configured
- Access to the target namespace (default: `sast-ai-mlops`)
- PostgreSQL database running with the orchestrator application

## Quick Start

Login to OpenShift and run the deployment script:

```bash
oc login <your-cluster>
./deploy-grafana-dashboards.sh
```

This script will:
1. Auto-generate dashboard ConfigMaps from JSON files in `../dashboards/`
2. Create/verify Grafana instance
3. Deploy dashboard ConfigMaps
4. Create PostgreSQL datasource
5. Deploy dashboard references

## Access Grafana

After deployment, access Grafana at:

```
http://sast-ai-grafana-service-sast-ai-mlops.apps.<your-cluster-domain>
```

Login with credentials: **admin/admin** (configured in `grafana-instance.yaml`)

## Available Dashboards

1. **MLOps Batch Metrics Overview** - Aggregate metrics across all batches
   - Latest batch metrics (accuracy, precision, recall, F1)
   - Performance trends over time
   - Confusion matrix for recent batches
   - Comparison by prompt version, known non-issues version, and container image

2. **MLOps Job Details** - Job-level metrics for individual packages
   - Job-level metrics distribution
   - Detailed job metrics table with confusion matrix values
   - Filterable by batch, job, and package name

## Architecture

### Dashboard Source of Truth
- Dashboard JSON files are stored in `../dashboards/`
- ConfigMaps are **auto-generated** during deployment (not committed to git)
- Edit dashboards in Grafana UI, export JSON, and save to `../dashboards/`

### How It Works
1. `generate-dashboard-configmaps.sh` reads JSON files from `../dashboards/`
2. Creates temporary ConfigMap YAML files: `configmap-*.yaml`
3. `deploy-grafana-dashboards.sh` applies these ConfigMaps to OpenShift
4. Dashboard CRDs (`grafana-dashboard-*.yaml`) reference these ConfigMaps
5. Grafana Operator syncs dashboards to Grafana instance

## Files Overview

### Committed Files
- `README.md` - This documentation
- `deploy-grafana-dashboards.sh` - Main deployment orchestrator
- `generate-dashboard-configmaps.sh` - Auto-generates ConfigMaps from JSON
- `grafana-instance.yaml` - Grafana instance CRD (Grafana app deployment)
- `grafana-datasource.yaml` - PostgreSQL datasource CRD
- `grafana-dashboard-batch-metrics.yaml` - Batch metrics dashboard reference
- `grafana-dashboard-job-details.yaml` - Job details dashboard reference

### Generated/Local Files (gitignored)
- `configmap-*.yaml` - Auto-generated from `../dashboards/*.json`
- `.env` - Local credentials (only needed for troubleshooting script)
- `fix-datasource-password.sh` - Emergency troubleshooting script

## Troubleshooting

### Dashboards Show "No Data"

**First, verify the datasource:**
1. Log into Grafana
2. Go to Configuration → Data sources
3. Click on "SAST-AI-PostgreSQL"
4. Click "Test" button
5. Should show "Database Connection OK"

**If the connection test fails:**

The Grafana Operator v5 has a known bug where datasource passwords may not persist properly during reconciliation cycles. If you encounter this issue repeatedly:

1. Create a local `.env` file with your credentials:
   ```bash
   # Grafana credentials
   GRAFANA_USERNAME=admin
   GRAFANA_PASSWORD=admin

   # PostgreSQL database credentials
   DB_USERNAME=quarkus
   DB_PASSWORD=quarkus

   # OpenShift namespace
   NAMESPACE=sast-ai-mlops

   # Grafana datasource UID (do not change unless recreating datasource)
   DATASOURCE_UID=c3340f63-6cc1-4a9e-9cd2-72876665193b
   ```

2. Use the troubleshooting script (not committed to git):
   ```bash
   ./fix-datasource-password.sh
   ```

   This script will:
   - Port-forward to Grafana service
   - Update datasource password via Grafana API
   - Test the database connection

**Note:** We've applied a fix to `grafana-datasource.yaml` to address the operator bug. If you still need to run the fix script frequently, please report this as it indicates the operator issue persists.

### Verify Deployment Status

```bash
# Check Grafana instance
oc get grafana -n sast-ai-mlops

# Check datasource
oc get grafanadatasource -n sast-ai-mlops

# Check dashboards
oc get grafanadashboard -n sast-ai-mlops

# Check dashboard ConfigMaps
oc get configmap -n sast-ai-mlops | grep dashboard
```

### Redeploy Everything

If you need to start fresh:

```bash
# Delete all Grafana resources
oc delete grafanadashboard --all -n sast-ai-mlops
oc delete grafanadatasource --all -n sast-ai-mlops
oc delete grafana --all -n sast-ai-mlops
oc delete configmap -l app=sast-ai-orchestrator -n sast-ai-mlops

# Redeploy
./deploy-grafana-dashboards.sh
```

## Making Dashboard Changes

1. **Edit in Grafana UI:**
   - Make your changes in the Grafana web interface
   - Test thoroughly

2. **Export the dashboard:**
   - Click dashboard settings (gear icon)
   - Click "JSON Model"
   - Copy the JSON

3. **Update the source file:**
   - Save to `../dashboards/mlops-batch-metrics.json` or `mlops-job-details.json`
   - Commit the JSON file to git

4. **Redeploy:**
   ```bash
   ./deploy-grafana-dashboards.sh
   ```

## Security Notes

- Default credentials (admin/admin) are configured in `grafana-instance.yaml`
- Database credentials are pulled from the existing Kubernetes secret: `sast-ai-orchestrator-postgresql`
- Change Grafana admin password in production environments
- The `.env` file (if created) contains credentials and is gitignored

## Technical Details

### Grafana Operator
- Version: v5.20.0 (check with `oc get csv -n openshift-operators | grep grafana`)
- Custom Resource Definitions (CRDs):
  - `Grafana` - Grafana instance
  - `GrafanaDatasource` - Data source connections
  - `GrafanaDashboard` - Dashboard references

### Database Connection
- **Service**: `sast-ai-orchestrator-postgresql:5432`
- **Database**: `sast-ai`
- **User**: `quarkus`
- **Password**: Pulled from secret `sast-ai-orchestrator-postgresql`
- **SSL Mode**: Disabled (cluster-internal connection)

### Datasource Configuration
The datasource uses the Grafana Operator's `valuesFrom` feature to securely inject the password from a Kubernetes secret. The password is never stored in plaintext in the YAML files.