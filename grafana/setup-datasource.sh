#!/bin/bash
# Script to configure Grafana PostgreSQL datasource via API
# Can be used for manual setup or CI/CD automation

set -e

# Configuration
GRAFANA_URL="${GRAFANA_URL:-http://localhost:3000}"
GRAFANA_USER="${GRAFANA_USER:-admin}"
GRAFANA_PASSWORD="${GRAFANA_PASSWORD:-admin}"

# Database configuration (update these for your environment)
DB_HOST="${DB_HOST:-postgres}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-sast-ai}"
DB_USER="${DB_USER:-quarkus}"
DB_PASSWORD="${DB_PASSWORD:-quarkus}"

echo "Waiting for Grafana to be ready..."
until curl -s "${GRAFANA_URL}/api/health" > /dev/null 2>&1; do
  sleep 2
done

echo "Grafana is ready. Configuring PostgreSQL datasource..."

# Delete existing datasource if it exists (optional)
curl -s -u "${GRAFANA_USER}:${GRAFANA_PASSWORD}" \
  -X DELETE "${GRAFANA_URL}/api/datasources/name/SAST-AI-PostgreSQL" || true

# Create datasource with proper password in secureJsonData
curl -s -u "${GRAFANA_USER}:${GRAFANA_PASSWORD}" \
  -X POST -H "Content-Type: application/json" \
  -d "{
    \"name\": \"SAST-AI-PostgreSQL\",
    \"type\": \"grafana-postgresql-datasource\",
    \"access\": \"proxy\",
    \"url\": \"${DB_HOST}:${DB_PORT}\",
    \"database\": \"${DB_NAME}\",
    \"user\": \"${DB_USER}\",
    \"secureJsonData\": {
      \"password\": \"${DB_PASSWORD}\"
    },
    \"jsonData\": {
      \"sslmode\": \"disable\",
      \"postgresVersion\": 1400,
      \"timescaledb\": false
    },
    \"isDefault\": true
  }" \
  "${GRAFANA_URL}/api/datasources"

echo ""
echo "✓ PostgreSQL datasource configured successfully!"
echo "  - Name: SAST-AI-PostgreSQL"
echo "  - URL: ${DB_HOST}:${DB_PORT}"
echo "  - Database: ${DB_NAME}"