# Detect OpenShift vs Kubernetes
KUBECTL_CMD := $(shell command -v oc 2>/dev/null || echo kubectl)

# Get current namespace from kubectl/oc, fallback to sast-ai-workflow
CURRENT_NAMESPACE := $(shell $(KUBECTL_CMD) config view --minify --output 'jsonpath={..namespace}' 2>/dev/null)
NAMESPACE ?= $(if $(CURRENT_NAMESPACE),$(CURRENT_NAMESPACE),sast-ai-workflow)

RELEASE_NAME ?= sast-ai-orchestrator
CHART_PATH ?= ./sast-ai-chart
HELM_TIMEOUT ?= 300s

.DEFAULT_GOAL := help

.PHONY: deploy
deploy: ## Deploy SAST AI to the cluster
	@echo "Deploying SAST AI..."
	@echo "Namespace: $(NAMESPACE)"
	@echo "Release: $(RELEASE_NAME)"
	@echo "Using: $(KUBECTL_CMD)"
	@helm repo add bitnami https://charts.bitnami.com/bitnami >/dev/null 2>&1 || true
	@helm repo update >/dev/null 2>&1
	@cd $(CHART_PATH) && helm dependency update
	@helm install $(RELEASE_NAME) $(CHART_PATH) \
		-f $(CHART_PATH)/values.yaml \
		-n $(NAMESPACE) \
		--create-namespace \
		--timeout=$(HELM_TIMEOUT) \
		--set app.env.SAST_AI_WORKFLOW_NAMESPACE=$(NAMESPACE)
	@echo "Deployment completed!"
	@$(MAKE) wait-pods
	@echo "Checking for ArgoCD availability..."
	@if $(KUBECTL_CMD) get crd applications.argoproj.io >/dev/null 2>&1; then \
		echo "ArgoCD detected! Creating ArgoCD Application..."; \
		$(MAKE) argocd; \
	else \
		echo "ArgoCD not available in cluster, skipping ArgoCD Application creation."; \
	fi
	@$(MAKE) show-access

.PHONY: upgrade
upgrade: ## Upgrade existing deployment
	@echo "Upgrading SAST AI..."
	@helm repo add bitnami https://charts.bitnami.com/bitnami >/dev/null 2>&1 || true
	@helm repo update >/dev/null 2>&1
	@cd $(CHART_PATH) && helm dependency update
	@helm upgrade $(RELEASE_NAME) $(CHART_PATH) \
		-f $(CHART_PATH)/values.yaml \
		-n $(NAMESPACE) \
		--timeout=$(HELM_TIMEOUT) \
		--set app.env.SAST_AI_WORKFLOW_NAMESPACE=$(NAMESPACE)
	@echo "Upgrade completed!"
	@$(MAKE) wait

.PHONY: install
install: deploy ## Alias for deploy

.PHONY: clean
clean: ## Remove the deployment
	@echo "Removing SAST AI deployment..."
	@echo "Checking for ArgoCD Application..."
	@if $(KUBECTL_CMD) get crd applications.argoproj.io >/dev/null 2>&1; then \
		if $(KUBECTL_CMD) get application $(RELEASE_NAME)-syncer -n $(NAMESPACE) >/dev/null 2>&1; then \
			echo "Removing ArgoCD Application..."; \
			echo "Removing finalizers to prevent hanging..."; \
			$(KUBECTL_CMD) patch application $(RELEASE_NAME)-syncer -n $(NAMESPACE) --type='merge' -p='{"metadata":{"finalizers":[]}}' 2>/dev/null || true; \
			$(KUBECTL_CMD) delete application $(RELEASE_NAME)-syncer -n $(NAMESPACE) --ignore-not-found=true; \
			echo "ArgoCD Application removed!"; \
		else \
			echo "ArgoCD Application '$(RELEASE_NAME)-syncer' not found in namespace '$(NAMESPACE)'"; \
		fi; \
	else \
		echo "ArgoCD not available in cluster, skipping ArgoCD Application removal."; \
	fi
	@if helm list -n $(NAMESPACE) | grep -q "^$(RELEASE_NAME)"; then \
		helm uninstall $(RELEASE_NAME) -n $(NAMESPACE) --timeout=$(HELM_TIMEOUT); \
		echo "Deployment removed!"; \
	else \
		echo "Release '$(RELEASE_NAME)' not found in namespace '$(NAMESPACE)'"; \
	fi
	@echo "Cleaning up remaining resources by label..."
	@$(KUBECTL_CMD) delete all,pvc,secrets,configmaps,ingress,routes,networkpolicy,pdb,sa,role,rolebinding,job -n $(NAMESPACE) -l "app.kubernetes.io/instance=$(RELEASE_NAME)" --ignore-not-found=true 2>/dev/null || true
	@echo "Force cleaning any remaining resources with release name prefix..."
	@for resource_type in deployment replicaset pod service configmap secret pvc role rolebinding serviceaccount networkpolicy poddisruptionbudget job cronjob ingress route; do \
		$(KUBECTL_CMD) get $$resource_type -n $(NAMESPACE) -o name 2>/dev/null | grep "$(RELEASE_NAME)" | xargs -r $(KUBECTL_CMD) delete -n $(NAMESPACE) --ignore-not-found=true 2>/dev/null || true; \
	done
	@echo "Cleanup completed!"

.PHONY: wait-pods
wait-pods: ## Wait for pods to be ready
	@echo "Waiting for SAST AI to be ready..."
	@echo "Checking PostgreSQL database..."
	@$(KUBECTL_CMD) wait --for=condition=ready pod -l app.kubernetes.io/name=postgresql -n $(NAMESPACE) --timeout=300s
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
	@NAMESPACE=$(NAMESPACE) envsubst < ./argocd/application.yaml | $(KUBECTL_CMD) apply -f -
	@echo "ArgoCD Application deployed to namespace: $(NAMESPACE)"

.PHONY: help
help: ## Show this help message
	@echo "SAST AI Deployment Helper"
	@echo ""
	@echo "  make deploy              # Deploy and wait until ready (sast-ai-workflow)"
	@echo "  make upgrade             # Upgrade and wait until ready"
	@echo "  make clean               # Remove deployment"
	@echo "  make argocd              # Deploy ArgoCD Application"
	@echo ""
