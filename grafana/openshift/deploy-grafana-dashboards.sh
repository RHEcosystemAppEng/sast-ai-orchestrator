#!/bin/bash
# Deploy Grafana dashboards to OpenShift using Grafana Operator

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NAMESPACE="${NAMESPACE:-sast-ai-mlops}"

echo "================================================"
echo "Deploying Grafana Dashboards to OpenShift"
echo "================================================"
echo ""

# Generate ConfigMaps from dashboard JSON files
echo "Generating dashboard ConfigMaps from source files..."
"${SCRIPT_DIR}/generate-dashboard-configmaps.sh"

echo ""
echo "Namespace: ${NAMESPACE}"
echo ""

# Check if oc is available
if ! command -v oc &> /dev/null; then
    echo "Error: oc CLI not found. Please install OpenShift CLI."
    exit 1
fi

# Check if logged in to OpenShift
if ! oc whoami &> /dev/null; then
    echo "Error: Not logged in to OpenShift. Please run 'oc login' first."
    exit 1
fi

# Check if namespace exists, create if not
if ! oc get namespace "${NAMESPACE}" &> /dev/null; then
    echo "Creating namespace: ${NAMESPACE}"
    oc create namespace "${NAMESPACE}"
fi

# Switch to namespace
oc project "${NAMESPACE}"

echo ""
echo "Step 1/5: Creating Grafana instance..."
if oc get grafana sast-ai-grafana &> /dev/null; then
    echo "  Grafana instance already exists"
else
    oc apply -f "${SCRIPT_DIR}/grafana-instance.yaml"
    echo "  ✓ Grafana instance created"
    echo "  Waiting for Grafana to be ready..."
    oc wait --for=condition=Ready grafana/sast-ai-grafana --timeout=300s
fi
echo "✓ Grafana instance ready"

echo ""
echo "Step 2/5: Creating dashboard ConfigMaps..."
oc apply -f "${SCRIPT_DIR}/configmap-batch-metrics.yaml"
oc apply -f "${SCRIPT_DIR}/configmap-job-details.yaml"
echo "✓ ConfigMaps created"

echo ""
echo "Step 3/5: Creating Grafana datasource..."
oc apply -f "${SCRIPT_DIR}/grafana-datasource.yaml"
echo "✓ Datasource created"

echo ""
echo "Step 4/5: Waiting for datasource to be synced..."
sleep 5
echo "✓ Datasource synced"

echo ""
echo "Step 5/5: Creating Grafana dashboards..."
oc apply -f "${SCRIPT_DIR}/grafana-dashboard-batch-metrics.yaml"
oc apply -f "${SCRIPT_DIR}/grafana-dashboard-job-details.yaml"
echo "✓ Dashboards created"

echo ""
echo "================================================"
echo "✓ Deployment completed successfully!"
echo "================================================"
echo ""
echo "Next steps:"
echo "1. Ensure your Grafana instance has the label 'dashboards: grafana'"
echo "2. Ensure PostgreSQL service is accessible at 'postgresql:5432'"
echo "3. Verify datasource connection in Grafana UI"
echo "4. Access your dashboards in Grafana"
echo ""
echo "To check deployment status:"
echo "  oc get grafanadatasource -n ${NAMESPACE}"
echo "  oc get grafanadashboard -n ${NAMESPACE}"
echo ""