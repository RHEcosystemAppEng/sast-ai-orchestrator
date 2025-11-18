#!/bin/bash

set -e  # Exit on any error

echo "========================================="
echo "SAST-AI Grafana Dashboard Setup"
echo "========================================="
echo ""

# Configuration
POSTGRES_HOST=${POSTGRES_HOST:-localhost}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
POSTGRES_DB=${POSTGRES_DB:-sast-ai}
POSTGRES_USER=${POSTGRES_USER:-quarkus}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-quarkus}
GRAFANA_PORT=${GRAFANA_PORT:-3000}
GRAFANA_ADMIN_USER=${GRAFANA_ADMIN_USER:-admin}
GRAFANA_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD:-admin}
DATASOURCE_UID="P413637974B2AAB20"  # Fixed UID to match dashboards

# Project root directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

echo "Step 1/6: Checking prerequisites..."
echo "-------------------------------------"

# Check if podman is available
if ! command -v podman &> /dev/null; then
    echo "❌ Error: podman is not installed or not in PATH"
    exit 1
fi

# Check if podman machine is running
if ! podman ps &> /dev/null; then
    echo "⚠️  Podman machine is not running. Starting..."
    podman machine start
    sleep 5
fi

# Check if PostgreSQL is accessible
echo "Checking PostgreSQL connection at ${POSTGRES_HOST}:${POSTGRES_PORT}..."
if ! PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c "SELECT 1;" &> /dev/null; then
    echo "❌ Error: Cannot connect to PostgreSQL at ${POSTGRES_HOST}:${POSTGRES_PORT}"
    echo "   Please ensure PostgreSQL is running and credentials are correct."
    exit 1
fi
echo "✓ PostgreSQL connection OK"
echo ""

echo "Step 2/6: Applying database migrations..."
echo "-------------------------------------"

# Apply migrations in order
MIGRATIONS=(
    "V001__initial_schema.sql"
    "V002__add_dvc_metadata_fields.sql"
    "V003__add_data_artifacts_id_column.sql"
    "V1_1_0__Add_OSH_Retry_Tables.sql"
    "V1_2_0__Add_Aggregate_Results_G_Sheet.sql"
    "V006__create_mlops_tables.sql"
    "V007__create_mlops_issue_details.sql"
    "V005__create_mlops_batch_metrics_view.sql"
)

for migration in "${MIGRATIONS[@]}"; do
    migration_path="$PROJECT_ROOT/src/main/resources/db/migration/$migration"
    if [ -f "$migration_path" ]; then
        echo "  Applying $migration..."
        PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -f "$migration_path" > /dev/null 2>&1 || true
    else
        echo "  ⚠️  Migration file not found: $migration (skipping)"
    fi
done
echo "✓ Database migrations applied"
echo ""

echo "Step 3/6: Starting Grafana container..."
echo "-------------------------------------"

# Stop and remove existing Grafana container if it exists
if podman ps -a --format "{{.Names}}" | grep -q "^sast-ai-grafana$"; then
    echo "  Stopping existing Grafana container..."
    podman stop sast-ai-grafana &> /dev/null || true
    podman rm sast-ai-grafana &> /dev/null || true
fi

# Determine the correct PostgreSQL URL for Grafana container
if [ "$POSTGRES_HOST" == "localhost" ] || [ "$POSTGRES_HOST" == "127.0.0.1" ]; then
    # If PostgreSQL is on localhost, use host.docker.internal for container access
    PG_URL="host.docker.internal:${POSTGRES_PORT}"
else
    PG_URL="${POSTGRES_HOST}:${POSTGRES_PORT}"
fi

echo "  Starting Grafana container..."
podman run -d \
  --name sast-ai-grafana \
  -e GF_SECURITY_ADMIN_USER=$GRAFANA_ADMIN_USER \
  -e GF_SECURITY_ADMIN_PASSWORD=$GRAFANA_ADMIN_PASSWORD \
  -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
  -p ${GRAFANA_PORT}:3000 \
  -v "$PROJECT_ROOT/grafana/provisioning:/etc/grafana/provisioning:Z" \
  -v "$PROJECT_ROOT/grafana/dashboards:/etc/grafana/dashboards:Z" \
  grafana/grafana:9.5.0 > /dev/null

echo "  Waiting for Grafana to start..."
for i in {1..30}; do
    if curl -s http://localhost:${GRAFANA_PORT}/api/health > /dev/null 2>&1; then
        echo "✓ Grafana started successfully"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Error: Grafana failed to start within 30 seconds"
        exit 1
    fi
    sleep 1
done
echo ""

echo "Step 4/6: Configuring PostgreSQL datasource..."
echo "-------------------------------------"

# Wait a bit more for Grafana to be fully ready
sleep 3

# Delete any existing datasources with the same name
echo "  Removing any existing datasources..."
EXISTING_DS=$(curl -s -u ${GRAFANA_ADMIN_USER}:${GRAFANA_ADMIN_PASSWORD} \
  http://localhost:${GRAFANA_PORT}/api/datasources/name/SAST-AI-PostgreSQL 2>/dev/null || echo "")

if echo "$EXISTING_DS" | grep -q '"id"'; then
    DS_ID=$(echo "$EXISTING_DS" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
    curl -s -X DELETE -u ${GRAFANA_ADMIN_USER}:${GRAFANA_ADMIN_PASSWORD} \
      http://localhost:${GRAFANA_PORT}/api/datasources/${DS_ID} > /dev/null
fi

# Create datasource with the correct UID
echo "  Creating PostgreSQL datasource..."
curl -s -X POST -H "Content-Type: application/json" -u ${GRAFANA_ADMIN_USER}:${GRAFANA_ADMIN_PASSWORD} \
  -d "{
    \"uid\": \"${DATASOURCE_UID}\",
    \"name\": \"SAST-AI-PostgreSQL\",
    \"type\": \"postgres\",
    \"access\": \"proxy\",
    \"url\": \"${PG_URL}\",
    \"database\": \"${POSTGRES_DB}\",
    \"user\": \"${POSTGRES_USER}\",
    \"secureJsonData\": {
      \"password\": \"${POSTGRES_PASSWORD}\"
    },
    \"jsonData\": {
      \"sslmode\": \"disable\",
      \"postgresVersion\": 1400,
      \"timescaledb\": false
    },
    \"isDefault\": true
  }" \
  http://localhost:${GRAFANA_PORT}/api/datasources > /dev/null

echo "✓ Datasource configured"
echo ""

echo "Step 5/6: Importing dashboards..."
echo "-------------------------------------"

# Import batch metrics dashboard
echo "  Importing MLOps Batch Metrics Overview..."
BATCH_DASHBOARD=$(cat "$PROJECT_ROOT/grafana/dashboards/mlops-batch-metrics.json")
curl -s -X POST -H "Content-Type: application/json" -u ${GRAFANA_ADMIN_USER}:${GRAFANA_ADMIN_PASSWORD} \
  -d "{
    \"dashboard\": ${BATCH_DASHBOARD},
    \"overwrite\": true,
    \"message\": \"Imported via setup script\"
  }" \
  http://localhost:${GRAFANA_PORT}/api/dashboards/db > /dev/null

echo "  Importing MLOps Job Details..."
JOB_DASHBOARD=$(cat "$PROJECT_ROOT/grafana/dashboards/mlops-job-details.json")
curl -s -X POST -H "Content-Type: application/json" -u ${GRAFANA_ADMIN_USER}:${GRAFANA_ADMIN_PASSWORD} \
  -d "{
    \"dashboard\": ${JOB_DASHBOARD},
    \"overwrite\": true,
    \"message\": \"Imported via setup script\"
  }" \
  http://localhost:${GRAFANA_PORT}/api/dashboards/db > /dev/null

echo "✓ Dashboards imported"
echo ""

echo "Step 6/6: Verifying setup..."
echo "-------------------------------------"

# Test datasource connection
DS_HEALTH=$(curl -s -X POST -u ${GRAFANA_ADMIN_USER}:${GRAFANA_ADMIN_PASSWORD} \
  http://localhost:${GRAFANA_PORT}/api/datasources/uid/${DATASOURCE_UID}/health)

if echo "$DS_HEALTH" | grep -q '"status":"OK"'; then
    echo "✓ Datasource connection verified"
else
    echo "⚠️  Warning: Datasource connection test failed"
fi

# Check for data
BATCH_COUNT=$(PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB \
  -t -c "SELECT COUNT(*) FROM mlops_batch;" 2>/dev/null | xargs)

if [ -n "$BATCH_COUNT" ] && [ "$BATCH_COUNT" -gt 0 ]; then
    echo "✓ Database contains $BATCH_COUNT batch(es)"
else
    echo "⚠️  Warning: No batches found in database"
    echo "   You may need to load data using the generate_mock_data.py script"
fi

echo ""
echo "========================================="
echo "✓ Setup Complete!"
echo "========================================="
echo ""
echo "Grafana is running at: http://localhost:${GRAFANA_PORT}"
echo "  Username: ${GRAFANA_ADMIN_USER}"
echo "  Password: ${GRAFANA_ADMIN_PASSWORD}"
echo ""
echo "Dashboards:"
echo "  - Batch Metrics: http://localhost:${GRAFANA_PORT}/d/mlops-batch-metrics"
echo "  - Job Details:   http://localhost:${GRAFANA_PORT}/d/mlops-job-details"
echo ""
echo "To stop Grafana:"
echo "  podman stop sast-ai-grafana"
echo ""
echo "To restart Grafana:"
echo "  podman start sast-ai-grafana"
echo ""