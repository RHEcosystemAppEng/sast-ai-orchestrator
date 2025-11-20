#!/bin/bash
# Script to import Grafana dashboards via API
# Can be used for manual setup or CI/CD automation

set -e

# Configuration
GRAFANA_URL="${GRAFANA_URL:-http://localhost:3000}"
GRAFANA_USER="${GRAFANA_USER:-admin}"
GRAFANA_PASSWORD="${GRAFANA_PASSWORD:-admin}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Importing dashboards to Grafana at ${GRAFANA_URL}..."

# Import MLOps Batch Metrics dashboard
echo "Importing MLOps Batch Metrics Overview..."
DASHBOARD_JSON=$(cat "${SCRIPT_DIR}/dashboards/mlops-batch-metrics.json")
curl -s -u "${GRAFANA_USER}:${GRAFANA_PASSWORD}" \
  -X POST -H "Content-Type: application/json" \
  -d "{\"dashboard\": ${DASHBOARD_JSON}, \"overwrite\": true}" \
  "${GRAFANA_URL}/api/dashboards/db" | \
  python3 -c "import sys, json; r=json.load(sys.stdin); print(f\"  ✓ {r.get('title', 'Dashboard')} imported: {r.get('url', 'N/A')}\")"

# Import MLOps Job Details dashboard
echo "Importing MLOps Job Details..."
DASHBOARD_JSON2=$(cat "${SCRIPT_DIR}/dashboards/mlops-job-details.json")
curl -s -u "${GRAFANA_USER}:${GRAFANA_PASSWORD}" \
  -X POST -H "Content-Type: application/json" \
  -d "{\"dashboard\": ${DASHBOARD_JSON2}, \"overwrite\": true}" \
  "${GRAFANA_URL}/api/dashboards/db" | \
  python3 -c "import sys, json; r=json.load(sys.stdin); print(f\"  ✓ {r.get('title', 'Dashboard')} imported: {r.get('url', 'N/A')}\")"

echo ""
echo "✓ All dashboards imported successfully!"
echo "  Access them at: ${GRAFANA_URL}"