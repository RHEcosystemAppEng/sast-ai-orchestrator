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
	@$(MAKE) wait

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
	@if helm list -n $(NAMESPACE) | grep -q "^$(RELEASE_NAME)"; then \
		helm uninstall $(RELEASE_NAME) -n $(NAMESPACE) --timeout=$(HELM_TIMEOUT); \
		echo "Deployment removed!"; \
	else \
		echo "Release '$(RELEASE_NAME)' not found in namespace '$(NAMESPACE)'"; \
	fi

.PHONY: wait
wait: ## Wait for deployment to be ready
	@echo "Waiting for SAST AI to be ready..."
	@echo "Checking PostgreSQL database..."
	@$(KUBECTL_CMD) wait --for=condition=ready pod -l app.kubernetes.io/name=postgresql -n $(NAMESPACE) --timeout=300s
	@echo "Database is ready!"
	@echo "Checking SAST AI application..."
	@$(KUBECTL_CMD) wait --for=condition=ready pod -l app.kubernetes.io/name=sast-ai -n $(NAMESPACE) --timeout=300s
	@echo "Application is ready!"
	@echo ""
	@echo "SAST AI is now fully ready to use!"
	@if [ "$(KUBECTL_CMD)" = "oc" ]; then \
		echo "Access URL:"; \
		$(KUBECTL_CMD) get route $(RELEASE_NAME) -n $(NAMESPACE) -o jsonpath='{.spec.host}' 2>/dev/null && echo "" || echo "No route found"; \
	fi

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

.PHONY: help
help: ## Show this help message
	@echo "SAST AI Deployment Helper"
	@echo ""
	@echo "  make deploy              # Deploy and wait until ready (sast-ai-workflow)"
	@echo "  make upgrade             # Upgrade and wait until ready"
	@echo "  make clean               # Remove deployment"
	@echo ""

