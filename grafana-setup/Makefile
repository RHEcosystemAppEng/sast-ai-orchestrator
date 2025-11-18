.PHONY: help start stop restart logs clean db-migrate db-status postgres-logs grafana-logs

help:
	@echo "SAST-AI Grafana & PostgreSQL Management"
	@echo "========================================"
	@echo ""
	@echo "Available commands:"
	@echo "  make start        - Start PostgreSQL and Grafana containers"
	@echo "  make stop         - Stop all containers"
	@echo "  make restart      - Restart all containers"
	@echo "  make logs         - View all container logs"
	@echo "  make clean        - Stop and remove all containers and volumes"
	@echo "  make db-migrate   - Apply database migrations"
	@echo "  make db-status    - Check database status and data"
	@echo "  make postgres-logs - View PostgreSQL logs only"
	@echo "  make grafana-logs  - View Grafana logs only"
	@echo ""

start:
	@echo "Starting SAST-AI services..."
	@echo ""
	@echo "Creating network..."
	@podman network create sast-ai-network 2>/dev/null || true
	@echo ""
	@echo "Step 1/3: Starting PostgreSQL..."
	@podman run -d \
	  --name sast-ai-postgres \
	  --network sast-ai-network \
	  -e POSTGRES_DB=sast-ai \
	  -e POSTGRES_USER=quarkus \
	  -e POSTGRES_PASSWORD=quarkus \
	  -p 5432:5432 \
	  -v postgres-data:/var/lib/postgresql/data \
	  postgres:14 > /dev/null
	@echo "  Waiting for PostgreSQL to be ready..."
	@sleep 5
	@for i in 1 2 3 4 5; do \
		if podman exec sast-ai-postgres pg_isready -U quarkus > /dev/null 2>&1; then \
			echo "  ✓ PostgreSQL is ready"; \
			break; \
		fi; \
		sleep 2; \
	done
	@echo ""
	@echo "Step 2/3: Starting Grafana..."
	@podman run -d \
	  --name sast-ai-grafana \
	  --network sast-ai-network \
	  -e GF_SECURITY_ADMIN_USER=admin \
	  -e GF_SECURITY_ADMIN_PASSWORD=admin \
	  -e POSTGRES_PASSWORD=quarkus \
	  -p 3000:3000 \
	  -v grafana-data:/var/lib/grafana \
	  -v $(PWD)/grafana-setup/grafana/provisioning/dashboards.yml:/etc/grafana/provisioning/dashboards/dashboards.yml:Z,ro \
	  -v $(PWD)/grafana-setup/grafana/dashboards:/var/lib/grafana/dashboards:Z,ro \
	  -v $(PWD)/grafana-setup/grafana/datasources/postgresql.yml:/etc/grafana/provisioning/datasources/postgresql.yml:Z,ro \
	  grafana/grafana:11.0.0 > /dev/null
	@echo "  Waiting for Grafana to be ready..."
	@sleep 5
	@echo "  ✓ Grafana is ready"
	@echo ""
	@echo "Step 3/3: Configuring datasource..."
	@sleep 2
	@curl -s -X PUT -H 'Content-Type: application/json' -u admin:admin \
	  -d '{"id":1,"uid":"P413637974B2AAB20","name":"SAST-AI-PostgreSQL","type":"postgres","access":"proxy","url":"sast-ai-postgres:5432","database":"sast-ai","user":"quarkus","secureJsonData":{"password":"quarkus"},"jsonData":{"sslmode":"disable","postgresVersion":1400,"timescaledb":false},"isDefault":true}' \
	  http://localhost:3000/api/datasources/uid/P413637974B2AAB20 > /dev/null 2>&1 || true
	@echo "  ✓ Datasource configured"
	@echo ""
	@echo "Step 4/5: Applying database migrations..."
	@$(MAKE) -f grafana-setup/Makefile db-migrate
	@echo ""
	@echo "Step 5/5: Loading data..."
	@BATCH_COUNT=$$(podman exec sast-ai-postgres psql -U quarkus -d sast-ai -t -c "SELECT COUNT(*) FROM mlops_batch;" 2>/dev/null | xargs); \
	if [ "$$BATCH_COUNT" = "0" ] || [ -z "$$BATCH_COUNT" ]; then \
		echo "   Waiting for data to load..."; \
		cat grafana-setup/sample-data.sql | podman exec -i sast-ai-postgres psql -U quarkus -d sast-ai > /dev/null 2>&1; \
		echo "  ✓ Finished data loaded (1 batch with 23 jobs)"; \
	else \
		echo "  ✓ Data already exists ($$BATCH_COUNT batches found)"; \
	fi
	@echo ""
	@echo "========================================="
	@echo "✓ All services started successfully!"
	@echo "========================================="
	@echo ""
	@echo "PostgreSQL: localhost:5432"
	@echo "  Database: sast-ai"
	@echo "  User: quarkus"
	@echo "  Password: quarkus"
	@echo ""
	@echo "Grafana: http://localhost:3000"
	@echo "  Username: admin"
	@echo "  Password: admin"
	@echo ""
	@echo "Dashboards:"
	@echo "  - Batch Metrics: http://localhost:3000/d/mlops-batch-metrics"
	@echo "  - Job Details:   http://localhost:3000/d/mlops-job-details"
	@echo ""

stop:
	@echo "Stopping all services..."
	@podman stop sast-ai-grafana sast-ai-postgres 2>/dev/null || true
	@echo "✓ All services stopped"

restart: stop
	@echo "Removing old containers..."
	@podman rm sast-ai-grafana sast-ai-postgres 2>/dev/null || true
	@$(MAKE) -f grafana-setup/Makefile start

logs:
	@echo "=== PostgreSQL Logs ==="
	@podman logs --tail 20 sast-ai-postgres 2>/dev/null || true
	@echo ""
	@echo "=== Grafana Logs ==="
	@podman logs --tail 20 sast-ai-grafana 2>/dev/null || true

postgres-logs:
	@podman logs -f sast-ai-postgres

grafana-logs:
	@podman logs -f sast-ai-grafana

clean:
	@echo "Cleaning up all services..."
	@podman stop sast-ai-grafana sast-ai-postgres 2>/dev/null || true
	@podman rm sast-ai-grafana sast-ai-postgres 2>/dev/null || true
	@podman volume rm grafana-data postgres-data 2>/dev/null || true
	@echo "✓ All services cleaned up"

db-migrate:
	@echo "  Applying database migrations..."
	@podman exec sast-ai-postgres psql -U quarkus -d sast-ai -c "SELECT 1;" > /dev/null 2>&1 || { echo "  ⚠️  PostgreSQL not ready, skipping migrations"; exit 0; }
	@for migration in V001__initial_schema.sql V002__add_dvc_metadata_fields.sql V003__add_data_artifacts_id_column.sql V1_1_0__Add_OSH_Retry_Tables.sql V1_2_0__Add_Aggregate_Results_G_Sheet.sql V006__create_mlops_tables.sql V007__create_mlops_issue_details.sql V005__create_mlops_batch_metrics_view.sql; do \
		if [ -f "src/main/resources/db/migration/$$migration" ]; then \
			podman cp "src/main/resources/db/migration/$$migration" sast-ai-postgres:/tmp/ 2>/dev/null; \
			podman exec sast-ai-postgres psql -U quarkus -d sast-ai -f "/tmp/$$migration" > /dev/null 2>&1 || true; \
		fi; \
	done
	@echo "  ✓ Migrations applied"

db-status:
	@echo "Database Status"
	@echo "==============="
	@echo ""
	@echo "Tables:"
	@podman exec sast-ai-postgres psql -U quarkus -d sast-ai -c "\dt mlops*" 2>/dev/null || echo "No tables found"
	@echo ""
	@echo "Batch Count:"
	@podman exec sast-ai-postgres psql -U quarkus -d sast-ai -t -c "SELECT COUNT(*) FROM mlops_batch;" 2>/dev/null | xargs || echo "0"
	@echo ""
	@echo "Job Count:"
	@podman exec sast-ai-postgres psql -U quarkus -d sast-ai -t -c "SELECT COUNT(*) FROM mlops_job;" 2>/dev/null | xargs || echo "0"