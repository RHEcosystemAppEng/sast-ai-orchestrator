# SAST AI OpenShift Deployment

This directory contains the Helm chart and deployment configurations for the SAST AI system on Red Hat OpenShift.

## 📁 Directory Structure

```
deploy/
├── README.md                    # This file - deployment documentation
├── deploy.sh                    # Deployment helper script
├── sast-ai-chart/             # Main Helm chart
│   ├── Chart.yaml             # Chart metadata
│   ├── values.yaml            # Configuration values
│   └── templates/             # Kubernetes manifests templates
│       ├── configmap.yaml     # Application configuration
│       ├── secret.yaml        # Database credentials
│       ├── deployment.yaml    # Application deployment
│       ├── service.yaml       # Application service
│       ├── route.yaml         # OpenShift route
│       ├── postgresql.yaml    # PostgreSQL database
│       ├── serviceaccount.yaml # Service account
│       ├── rbac.yaml          # RBAC permissions
│       ├── hpa.yaml           # Horizontal Pod Autoscaler
│       └── NOTES.txt          # Post-installation instructions
└── argocd/                    # ArgoCD configurations
    └── application.yaml       # ArgoCD application
```

## 🚀 Quick Start

### Prerequisites

- Red Hat OpenShift cluster (4.x+)
- Helm 3.x installed
- OpenShift CLI (`oc`) configured to access your cluster
- (Optional) ArgoCD installed for GitOps deployment

### Manual Deployment

1. **Deploy with default settings:**
   ```bash
   cd deploy
   ./deploy.sh
   ```

2. **Deploy to custom namespace:**
   ```bash
   ./deploy.sh -n sast-ai-workflow
   ```

3. **Upgrade existing deployment:**
   ```bash
   ./deploy.sh -u
   ```

4. **Dry run deployment:**
   ```bash
   ./deploy.sh -d
   ```

### ArgoCD GitOps Deployment

**Prerequisites**: ArgoCD must be installed and running in your cluster first.

1. **Install ArgoCD (if not already installed):**
   ```bash
   # Install ArgoCD Operator (recommended for OpenShift)
   oc apply -f https://raw.githubusercontent.com/argoproj-labs/argocd-operator/master/deploy/install.yaml
   
   # Or install ArgoCD directly
   oc create namespace argocd
   oc apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
   ```

2. **Wait for ArgoCD to be ready:**
   ```bash
   oc wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd
   ```

3. **Apply ArgoCD Application:**
   ```bash
   oc apply -f argocd/application.yaml
   ```

4. **ArgoCD will automatically:**
   - Monitor the `deploy/` directory for changes
   - Deploy updates when Helm chart or values change
   - Provide a UI for monitoring deployment status

5. **Access ArgoCD UI (optional):**
   ```bash
   # Get admin password
   oc get secret argocd-initial-admin-secret -n argocd -o jsonpath="{.data.password}" | base64 -d
   
   # Port forward to access UI
   oc port-forward svc/argocd-server -n argocd 8080:443
   ```

## 🏗️ Architecture

The deployment includes:

- **SAST AI Application**: Quarkus-based Java application
- **PostgreSQL Database**: Primary data store
- **Storage Options**: Filesystem or MinIO S3-compatible storage
- **OpenShift Route**: External access with TLS termination
- **Service Account**: With necessary RBAC permissions for Tekton integration
- **ConfigMap**: Application configuration
- **Secret**: Database credentials and storage credentials
- **HPA**: Auto-scaling based on CPU usage (optional)

## ⚙️ Configuration

### Key Configuration Options

| Parameter | Description | Default |
|-----------|-------------|---------|
| `app.image.repository` | Application Docker image | `sast-ai` |
| `app.image.tag` | Image tag | `latest` |
| `app.replicas` | Number of application pods | `1` |
| `postgresql.enabled` | Deploy PostgreSQL | `true` |
| `postgresql.auth.database` | Database name | `sast-ai` |
| `minio.enabled` | Enable MinIO S3 storage | `false` |
| `minio.storage.size` | Storage per MinIO server | `10Gi` |
| `route.enabled` | Enable OpenShift route | `true` |
| `route.tls.enabled` | Enable TLS on route | `true` |

### Customizing Values

Edit `deploy/sast-ai-chart/values.yaml` to customize the deployment:

```yaml
app:
  image:
    repository: quay.io/your-org/sast-ai
    tag: "v1.0.0"
  replicas: 2

route:
  host: "sast-ai.apps.your-cluster.com"

postgresql:
  auth:
    password: "your-secure-password"

minio:
  enabled: true
  servers: 4
  storage:
    size: 50Gi
  buckets:
    datasets: "sast-datasets"
    temp: "sast-temp"
```

### Database Configuration

The chart includes PostgreSQL by default. To use external database:
1. Set `postgresql.enabled: false`
2. Update database connection in `externalDatabase` section

## 🔍 Monitoring & Health Checks

- **Health Check Endpoint**: `/health`
- **Storage Health**: `/api/v1/dataset-storage/health`
- **Storage Info**: `/api/v1/dataset-storage/info`
- **Readiness Probe**: Configured to check application readiness
- **Liveness Probe**: Monitors application health
- **Route**: Provides external access with automatic hostname

## 🔒 Security

- Service account with minimal required permissions
- RBAC configured for Tekton integration
- Database credentials stored in Kubernetes Secret
- TLS enabled on OpenShift Route by default
- Compatible with OpenShift Security Context Constraints

## 🐛 Troubleshooting

### Common Issues

1. **Pod not starting:**
   ```bash
   oc describe pod -l app=sast-ai -n sast-ai-workflow
   oc logs -l app=sast-ai -n sast-ai-workflow
   ```

2. **Database connection issues:**
   ```bash
   oc exec -it deployment/sast-ai -n sast-ai-workflow -- env | grep DATABASE
   ```

3. **Check route connectivity:**
   ```bash
   oc get route sast-ai -n sast-ai-workflow
   export ROUTE_HOST=$(oc get route sast-ai -n sast-ai-workflow -o jsonpath='{.spec.host}')
   curl https://$ROUTE_HOST/health
   ```

4. **Check storage configuration:**
   ```bash
   # Check storage health and type
   curl https://$ROUTE_HOST/api/v1/dataset-storage/health
   curl https://$ROUTE_HOST/api/v1/dataset-storage/info

   # For MinIO deployments
   oc get tenant -n sast-ai-workflow
   oc get secrets -l component=minio-storage -n sast-ai-workflow
   ```

### Logs Access

```bash
# Application logs
oc logs -f deployment/sast-ai -n sast-ai-workflow

# Database logs
oc logs -f deployment/sast-ai-postgresql -n sast-ai-workflow
```

## 📚 OpenShift Commands

```bash
# Scale the application
oc scale deployment sast-ai --replicas=3 -n sast-ai-workflow

# Restart the application
oc rollout restart deployment sast-ai -n sast-ai-workflow

# View all resources
oc get all -l app.kubernetes.io/instance=sast-ai -n sast-ai-workflow

# Access application via route
oc get route sast-ai -n sast-ai-workflow
```

## 🤝 Contributing

When making changes to the Helm chart:

1. Test changes in development environment first
2. Update `values.yaml` if adding new features
3. Update this README if adding new functionality
4. Test ArgoCD sync if using GitOps

## 📖 Additional Resources

- [Helm Documentation](https://helm.sh/docs/)
- [OpenShift Documentation](https://docs.openshift.com/)
- [ArgoCD Documentation](https://argo-cd.readthedocs.io/)
- [Quarkus on OpenShift](https://quarkus.io/guides/deploying-to-openshift)

For questions or issues, please create an issue in the repository. 