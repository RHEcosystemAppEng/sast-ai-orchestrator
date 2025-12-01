#!/bin/bash

# Script to auto-generate Kubernetes ConfigMaps from Grafana dashboard JSON files
# This ensures dashboards are the single source of truth

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DASHBOARDS_DIR="$SCRIPT_DIR/../dashboards"
OUTPUT_DIR="$SCRIPT_DIR"

echo "=== Generating Dashboard ConfigMaps ==="
echo ""

# Check if dashboards directory exists
if [ ! -d "$DASHBOARDS_DIR" ]; then
    echo "Error: Dashboards directory not found at $DASHBOARDS_DIR"
    exit 1
fi

# Generate ConfigMap for each dashboard JSON file
for dashboard_file in "$DASHBOARDS_DIR"/*.json; do
    if [ ! -f "$dashboard_file" ]; then
        echo "No dashboard files found in $DASHBOARDS_DIR"
        exit 1
    fi

    dashboard_name=$(basename "$dashboard_file" .json)
    configmap_name="dashboard-${dashboard_name}"
    output_file="$OUTPUT_DIR/configmap-${dashboard_name}.yaml"

    echo "Generating ConfigMap: $configmap_name"
    echo "  Source: $dashboard_file"
    echo "  Output: $output_file"

    # Create ConfigMap YAML
    cat > "$output_file" << EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: $configmap_name
  labels:
    app: sast-ai-orchestrator
data:
  ${dashboard_name}.json: |-
EOF

    # Append dashboard JSON with proper indentation (4 spaces)
    sed 's/^/    /' "$dashboard_file" >> "$output_file"

    echo "  ✓ Generated"
    echo ""
done

echo "=== ConfigMaps generated successfully ==="
echo ""
echo "Generated files:"
ls -lh "$OUTPUT_DIR"/configmap-*.yaml
echo ""
echo "Note: These are temporary files. Do not commit them to git."
