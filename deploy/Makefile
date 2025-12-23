# Detect OpenShift vs Kubernetes
KUBECTL_CMD := $(shell command -v oc 2>/dev/null || echo kubectl)

# Environment-specific settings
DEV_NAMESPACE := sast-ai-dev
PROD_NAMESPACE := sast-ai-prod

# Get current namespace from kubectl/oc, fallback to dev
CURRENT_NAMESPACE := $(shell $(KUBECTL_CMD) config view --minify --output 'jsonpath={..namespace}' 2>/dev/null)
NAMESPACE ?= $(if $(CURRENT_NAMESPACE),$(CURRENT_NAMESPACE),$(DEV_NAMESPACE))

RELEASE_NAME ?= sast-ai-orchestrator
CHART_PATH ?= ./sast-ai-chart
HELM_TIMEOUT ?= 300s

# Secrets configuration
SECRETS_DIR ?= ./secrets
S3_CREDENTIALS_FILE ?= $(SECRETS_DIR)/s3-credentials.env
GOOGLE_SA_FILE ?= $(SECRETS_DIR)/google-service-account.json

.DEFAULT_GOAL := help

## ============================================================================
## Secrets Management
## ============================================================================

.PHONY: check-secrets
check-secrets: ## Verify that required secret files exist
	@echo "🔐 Checking for secret files..."
	@if [ ! -f "$(S3_CREDENTIALS_FILE)" ]; then \
		echo "❌ Error: $(S3_CREDENTIALS_FILE) not found!"; \
		echo "   Please copy $(S3_CREDENTIALS_FILE).example and fill in your credentials."; \
		echo "   See $(SECRETS_DIR)/README.md for instructions."; \
		exit 1; \
	fi
	@if [ ! -f "$(GOOGLE_SA_FILE)" ]; then \
		echo "❌ Error: $(GOOGLE_SA_FILE) not found!"; \
		echo "   Please copy $(GOOGLE_SA_FILE).example and fill in your service account JSON."; \
		echo "   See $(SECRETS_DIR)/README.md for instructions."; \
		exit 1; \
	fi
	@echo "✅ All required secret files found"

.PHONY: create-secrets
create-secrets: check-secrets ## Create Kubernetes secrets from local files
	@echo "🔐 Creating secrets in namespace: $(NAMESPACE)"
	@# Create namespace if it doesn't exist
	@$(KUBECTL_CMD) create namespace $(NAMESPACE) 2>/dev/null || true
	@# Create S3 credentials secret with properly mapped keys
	@echo "  Creating sast-ai-s3-credentials secret..."
	@set -a; . $(S3_CREDENTIALS_FILE); set +a; \
	$(KUBECTL_CMD) create secret generic sast-ai-s3-credentials \
		-n $(NAMESPACE) \
		--from-literal=access_key_id="$$AWS_ACCESS_KEY_ID" \
		--from-literal=secret_access_key="$$AWS_SECRET_ACCESS_KEY" \
		--from-literal=endpoint_url="$$AWS_S3_ENDPOINT_URL" \
		--dry-run=client -o yaml | $(KUBECTL_CMD) apply -f -
	@# Create Google Service Account secret
	@echo "  Creating sast-ai-google-service-account secret..."
	@$(KUBECTL_CMD) create secret generic sast-ai-google-service-account \
		-n $(NAMESPACE) \
		--from-file=service_account.json=$(GOOGLE_SA_FILE) \
		--dry-run=client -o yaml | $(KUBECTL_CMD) apply -f -
	@echo "✅ Secrets created successfully"

.PHONY: delete-secrets
delete-secrets: ## Delete Kubernetes secrets from namespace
	@echo "🗑️  Deleting secrets from namespace: $(NAMESPACE)"
	@$(KUBECTL_CMD) delete secret sast-ai-s3-credentials -n $(NAMESPACE) --ignore-not-found=true
	@$(KUBECTL_CMD) delete secret sast-ai-google-service-account -n $(NAMESPACE) --ignore-not-found=true
	@echo "✅ Secrets deleted"

.PHONY: rotate-secrets
rotate-secrets: delete-secrets create-secrets ## Rotate secrets (delete and recreate)
	@echo "✅ Secrets rotated successfully"

## ============================================================================
## Deployment
## ============================================================================

.PHONY: deploy-dev
deploy-dev: ## Deploy to development environment (sast-ai-dev namespace)
	@echo "🚀 Deploying SAST AI to DEVELOPMENT environment..."
	@echo "Namespace: $(DEV_NAMESPACE)"
	@echo "Release: $(RELEASE_NAME)-dev"
	@echo "Using: $(KUBECTL_CMD)"
	@$(MAKE) _deploy NAMESPACE=$(DEV_NAMESPACE) RELEASE_NAME=$(RELEASE_NAME)-dev VALUES_FILE=values-dev.yaml

.PHONY: deploy-prod
deploy-prod: ## Deploy to production environment (sast-ai-prod namespace)
	@echo "🚀 Deploying SAST AI to PRODUCTION environment..."
	@echo "Namespace: $(PROD_NAMESPACE)"
	@echo "Release: $(RELEASE_NAME)-prod"
	@echo "Using: $(KUBECTL_CMD)"
	@$(MAKE) _deploy NAMESPACE=$(PROD_NAMESPACE) RELEASE_NAME=$(RELEASE_NAME)-prod VALUES_FILE=values-prod.yaml

.PHONY: _deploy
_deploy: ## Internal deployment target
	@cd $(CHART_PATH) && helm dependency update
	@helm install $(RELEASE_NAME) $(CHART_PATH) \
		-f $(CHART_PATH)/values.yaml \
		-f $(CHART_PATH)/$(VALUES_FILE) \
		-n $(NAMESPACE) \
		--create-namespace \
		--timeout=$(HELM_TIMEOUT) \
		--set app.env.SAST_AI_WORKFLOW_NAMESPACE=$(NAMESPACE)
	@echo "✅ Deployment completed!"
	@$(MAKE) wait-pods NAMESPACE=$(NAMESPACE)
	@echo "🔍 Checking for ArgoCD availability..."
	@if $(KUBECTL_CMD) get crd applications.argoproj.io >/dev/null 2>&1; then \
		echo "📦 ArgoCD detected! Creating ArgoCD Application..."; \
		$(MAKE) argocd NAMESPACE=$(NAMESPACE); \
	else \
		echo "⚠️  ArgoCD not available in cluster, skipping ArgoCD Application creation."; \
	fi
	@$(MAKE) show-access NAMESPACE=$(NAMESPACE)

.PHONY: upgrade-dev
upgrade-dev: ## Upgrade development deployment
	@echo "⬆️  Upgrading SAST AI DEVELOPMENT deployment..."
	@$(MAKE) _upgrade NAMESPACE=$(DEV_NAMESPACE) RELEASE_NAME=$(RELEASE_NAME)-dev VALUES_FILE=values-dev.yaml

.PHONY: upgrade-prod
upgrade-prod: ## Upgrade production deployment
	@echo "⬆️  Upgrading SAST AI PRODUCTION deployment..."
	@$(MAKE) _upgrade NAMESPACE=$(PROD_NAMESPACE) RELEASE_NAME=$(RELEASE_NAME)-prod VALUES_FILE=values-prod.yaml

.PHONY: _upgrade
_upgrade: ## Internal upgrade target
	@cd $(CHART_PATH) && helm dependency update
	@helm upgrade $(RELEASE_NAME) $(CHART_PATH) \
		-f $(CHART_PATH)/values.yaml \
		-f $(CHART_PATH)/$(VALUES_FILE) \
		-n $(NAMESPACE) \
		--timeout=$(HELM_TIMEOUT) \
		--set app.env.SAST_AI_WORKFLOW_NAMESPACE=$(NAMESPACE)
	@echo "✅ Upgrade completed!"
	@$(MAKE) wait-pods NAMESPACE=$(NAMESPACE)

.PHONY: clean-dev
clean-dev: ## Remove development deployment
	@echo "🧹 Removing SAST AI DEVELOPMENT deployment..."
	@$(MAKE) _clean NAMESPACE=$(DEV_NAMESPACE) RELEASE_NAME=$(RELEASE_NAME)-dev

.PHONY: clean-prod
clean-prod: ## Remove production deployment
	@echo "🧹 Removing SAST AI PRODUCTION deployment..."
	@$(MAKE) _clean NAMESPACE=$(PROD_NAMESPACE) RELEASE_NAME=$(RELEASE_NAME)-prod

.PHONY: _clean
_clean: ## Internal clean target
	@echo "Cleaning up deployment $(RELEASE_NAME) in namespace $(NAMESPACE)"
	@if helm list -n $(NAMESPACE) | grep -q "^$(RELEASE_NAME)"; then \
		helm uninstall $(RELEASE_NAME) -n $(NAMESPACE) --timeout=$(HELM_TIMEOUT); \
		echo "Cleaning up remaining resources by label..."; \
		$(KUBECTL_CMD) delete all,pvc,secrets,configmaps,ingress,routes,networkpolicy,pdb,sa,role,rolebinding,job -n $(NAMESPACE) -l "app.kubernetes.io/instance=$(RELEASE_NAME)" --ignore-not-found=true 2>/dev/null || true; \
		echo "Force cleaning any remaining resources with release name prefix..."; \
		for resource_type in deployment replicaset pod service configmap secret pvc role rolebinding serviceaccount networkpolicy poddisruptionbudget job cronjob ingress route; do \
			$(KUBECTL_CMD) get $$resource_type -n $(NAMESPACE) -o name 2>/dev/null | grep "$(RELEASE_NAME)" | xargs -r $(KUBECTL_CMD) delete -n $(NAMESPACE) --ignore-not-found=true 2>/dev/null || true; \
		done; \
		echo "Cleanup completed!"; \
		echo "✅ Deployment removed from $(NAMESPACE)"; \
	else \
		echo "Release '$(RELEASE_NAME)' not found in namespace '$(NAMESPACE)'"; \
	fi

.PHONY: status
status: ## Show deployment status
	@echo "📊 SAST AI Deployment Status"
	@echo "============================"
	@echo ""
	@echo "🔍 Development Environment ($(DEV_NAMESPACE)):"
	@helm status $(RELEASE_NAME)-dev -n $(DEV_NAMESPACE) 2>/dev/null || echo "  ❌ Not deployed"
	@echo ""
	@echo "🔍 Production Environment ($(PROD_NAMESPACE)):"
	@helm status $(RELEASE_NAME)-prod -n $(PROD_NAMESPACE) 2>/dev/null || echo "  ❌ Not deployed"

.PHONY: wait-pods
wait-pods: ## Wait for pods to be ready
	@echo "Waiting for SAST AI to be ready..."
	@echo "Checking CloudNativePG PostgreSQL cluster..."
	@$(KUBECTL_CMD) wait --for=condition=ready cluster $(RELEASE_NAME)-db -n $(NAMESPACE) --timeout=300s 2>/dev/null || \
		$(KUBECTL_CMD) wait --for=condition=ready pod -l cnpg.io/cluster=$(RELEASE_NAME)-db -n $(NAMESPACE) --timeout=300s
	@echo "Database is ready!"
	@echo "Checking SAST AI application..."
	@$(KUBECTL_CMD) wait --for=condition=ready pod -l app.kubernetes.io/name=sast-ai -n $(NAMESPACE) --timeout=300s
	@echo "Application is ready!"

.PHONY: show-access
show-access: ## Show access information
	@echo ""
	@echo "SAST AI is now fully ready to use!"
	@if [[ "$(KUBECTL_CMD)" == *"oc" ]]; then \
		echo ""; \
		echo "=== ACCESS INFORMATION ==="; \
		ROUTE_HOST=$$($(KUBECTL_CMD) get route $(RELEASE_NAME) -n $(NAMESPACE) -o jsonpath='{.spec.host}' 2>/dev/null); \
		if [ -n "$$ROUTE_HOST" ]; then \
			ROUTE_TLS=$$($(KUBECTL_CMD) get route $(RELEASE_NAME) -n $(NAMESPACE) -o jsonpath='{.spec.tls}' 2>/dev/null); \
			if [ -n "$$ROUTE_TLS" ]; then \
				echo "🌐 Application URL: https://$$ROUTE_HOST"; \
			else \
				echo "🌐 Application URL: http://$$ROUTE_HOST"; \
			fi; \
		else \
			echo "⚠️  No route found. Check route configuration."; \
		fi; \
		echo "=========================="; \
	fi

.PHONY: wait
wait: wait-pods show-access ## Wait for deployment to be ready and show access info

.PHONY: dry-run
dry-run: ## Show what would be deployed (dry run)
	@echo "Dry run deployment preview"
	@helm install $(RELEASE_NAME) $(CHART_PATH) \
		-f $(CHART_PATH)/values.yaml \
		-n $(NAMESPACE) \
		--dry-run \
		--debug \
		--set app.env.SAST_AI_WORKFLOW_NAMESPACE=$(NAMESPACE)

.PHONY: template
template: ## Generate Kubernetes YAML templates
	@echo "Generating Kubernetes templates"
	@helm template $(RELEASE_NAME) $(CHART_PATH) \
		-f $(CHART_PATH)/values.yaml \
		-n $(NAMESPACE) \
		--set app.env.SAST_AI_WORKFLOW_NAMESPACE=$(NAMESPACE)

.PHONY: lint
lint: ## Lint the Helm chart
	@echo "Linting Helm chart"
	@helm lint $(CHART_PATH)

.PHONY: argocd
argocd: ## Deploy ArgoCD Application to current namespace
	@echo "Deploying ArgoCD Application..."
	@NAMESPACE=$(NAMESPACE) envsubst < argocd/application.yaml | $(KUBECTL_CMD) apply -f -
	@echo "ArgoCD Application deployed to namespace: $(NAMESPACE)"

.PHONY: help
help: ## Show this help message
	@echo "SAST AI Deployment Commands"
	@echo "============================"
	@echo ""
	@echo "Environment-specific deployments:"
	@echo "  deploy-dev     Deploy to development environment (sast-ai-dev namespace)"
	@echo "  deploy-prod    Deploy to production environment (sast-ai-prod namespace)"
	@echo "  upgrade-dev    Upgrade development deployment"
	@echo "  upgrade-prod   Upgrade production deployment"
	@echo "  clean-dev      Remove development deployment"
	@echo "  clean-prod     Remove production deployment"
	@echo ""
	@echo "Secrets management:"
	@echo "  check-secrets     Verify that required secret files exist"
	@echo "  create-secrets    Create Kubernetes secrets from local files"
	@echo "  delete-secrets    Delete Kubernetes secrets"
	@echo "  rotate-secrets    Rotate secrets (delete and recreate)"
	@echo ""
	@echo "Utility commands:"
	@echo "  status            Show deployment status for both environments"
	@echo "  argocd            Deploy ArgoCD Application"
	@echo ""
	@echo "Other commands:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-18s %s\n", $$1, $$2}' $(MAKEFILE_LIST)
