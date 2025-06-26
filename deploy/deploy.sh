#!/bin/bash

# SAST AI OpenShift Deployment Script
# This script helps deploy the SAST AI application using Helm on OpenShift

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
NAMESPACE="sast-ai"
CHART_PATH="./sast-ai-chart"
RELEASE_NAME="sast-ai"
UPGRADE=false
DELETE=false

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Deploy SAST AI application using Helm on OpenShift

OPTIONS:
    -n, --namespace NAMESPACE       Kubernetes namespace [default: sast-ai]
    -r, --release-name NAME         Helm release name [default: sast-ai]
    -u, --upgrade                   Upgrade existing deployment
    -d, --delete                    Delete/uninstall and remove all resources
    -h, --help                      Show this help message

EXAMPLES:
    $0                              Deploy with default settings
    $0 -u                           Upgrade existing deployment
    $0 -d                           Remove all deployed resources
    $0 -n my-sast-ai                Deploy to custom namespace

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -r|--release-name)
            RELEASE_NAME="$2"
            shift 2
            ;;
        -u|--upgrade)
            UPGRADE=true
            shift
            ;;
        -d|--delete)
            DELETE=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Check if Helm is installed
if ! command -v helm &> /dev/null; then
    print_error "Helm is not installed. Please install Helm first."
    exit 1
fi

# Check if oc or kubectl is installed
if ! command -v oc &> /dev/null && ! command -v kubectl &> /dev/null; then
    print_error "Neither 'oc' nor 'kubectl' is installed. Please install OpenShift CLI or kubectl first."
    exit 1
fi

# Prefer oc over kubectl for OpenShift
if command -v oc &> /dev/null; then
    KUBE_CMD="oc"
else
    KUBE_CMD="kubectl"
fi

# Handle delete operation
if [[ "$DELETE" == true ]]; then
    print_warning "This will uninstall the SAST AI application and remove all resources!"
    print_info "Namespace: $NAMESPACE"
    print_info "Release name: $RELEASE_NAME"
    print_info "Using command: $KUBE_CMD"
    
    # Confirm delete
    read -p "Are you sure you want to proceed? (y/N): " confirm
    if [[ $confirm != [yY] && $confirm != [yY][eE][sS] ]]; then
        print_info "Delete cancelled."
        exit 0
    fi
    
    # Uninstall Helm release
    print_info "Uninstalling Helm release: $RELEASE_NAME"
    if helm list -n $NAMESPACE | grep -q $RELEASE_NAME; then
        helm uninstall $RELEASE_NAME -n $NAMESPACE
        print_info "Helm release uninstalled successfully!"
    else
        print_warning "Helm release '$RELEASE_NAME' not found in namespace '$NAMESPACE'"
    fi
    
    # Delete namespace if it exists and is empty
    if $KUBE_CMD get namespace $NAMESPACE &> /dev/null; then
        print_info "Checking if namespace '$NAMESPACE' can be deleted..."
        RESOURCES=$($KUBE_CMD get all -n $NAMESPACE --no-headers 2>/dev/null | wc -l)
        if [[ $RESOURCES -eq 0 ]]; then
            print_info "Deleting empty namespace: $NAMESPACE"
            $KUBE_CMD delete namespace $NAMESPACE
        else
            print_warning "Namespace '$NAMESPACE' contains other resources, not deleting it"
            print_info "Remaining resources in namespace:"
            $KUBE_CMD get all -n $NAMESPACE
        fi
    else
        print_warning "Namespace '$NAMESPACE' not found"
    fi
    
    print_info "Delete completed!"
    exit 0
fi

# Check if chart directory exists
if [[ ! -d "$CHART_PATH" ]]; then
    print_error "Chart directory '$CHART_PATH' does not exist"
    exit 1
fi

print_info "Deploying SAST AI to OpenShift"
print_info "Namespace: $NAMESPACE"
print_info "Release name: $RELEASE_NAME"
print_info "Using command: $KUBE_CMD"

# Add PostgreSQL Helm repository
print_info "Adding PostgreSQL Helm repository..."
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Update Helm dependencies
print_info "Updating Helm dependencies..."
cd "$CHART_PATH"
helm dependency update
cd ..

# Build Helm command
HELM_CMD="helm"
if [[ "$UPGRADE" == true ]]; then
    HELM_CMD="$HELM_CMD upgrade"
else
    HELM_CMD="$HELM_CMD install"
fi

HELM_CMD="$HELM_CMD $RELEASE_NAME $CHART_PATH"
HELM_CMD="$HELM_CMD -f $CHART_PATH/values.yaml"
HELM_CMD="$HELM_CMD -n $NAMESPACE"
HELM_CMD="$HELM_CMD --create-namespace"

print_info "Deploying application..."

# Execute Helm command
print_info "Executing: $HELM_CMD"
eval $HELM_CMD

print_info "Deployment completed successfully!"

# Wait for deployment to be ready
print_info "Waiting for deployment to be ready..."
$KUBE_CMD wait --for=condition=available --timeout=300s deployment/$RELEASE_NAME -n $NAMESPACE

# Show deployment status
print_info "Deployment status:"
$KUBE_CMD get all -l app.kubernetes.io/instance=$RELEASE_NAME -n $NAMESPACE

# Show route information
if [[ "$KUBE_CMD" == "oc" ]]; then
    print_info "Application route:"
    $KUBE_CMD get route $RELEASE_NAME -n $NAMESPACE
fi

# Show access information
print_info "Application deployed successfully!"
print_info "Run 'helm status $RELEASE_NAME -n $NAMESPACE' for more information" 