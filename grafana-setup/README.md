# Grafana Dashboard Setup

## Quick Start

To start PostgreSQL and Grafana with all dashboards configured:

```bash
make -f Makefile.grafana start
```

Access Grafana at **http://localhost:3000**
- Username: `admin`
- Password: `admin`

## Available Commands

```bash
make -f Makefile.grafana help          # Show all available commands
make -f Makefile.grafana start         # Start PostgreSQL and Grafana
make -f Makefile.grafana stop          # Stop all containers
make -f Makefile.grafana restart       # Restart all containers
make -f Makefile.grafana logs          # View all container logs
make -f Makefile.grafana clean         # Stop and remove everything
make -f Makefile.grafana db-migrate    # Apply database migrations
make -f Makefile.grafana db-status     # Check database status
make -f Makefile.grafana postgres-logs # View PostgreSQL logs only
make -f Makefile.grafana grafana-logs  # View Grafana logs only
```

## Dashboards

After starting, the following dashboards will be available:

- **MLOps Batch Metrics**: http://localhost:3000/d/mlops-batch-metrics
- **MLOps Job Details**: http://localhost:3000/d/mlops-job-details

## What Gets Configured

The `make start` command automatically:
- ✓ Starts PostgreSQL container (postgres:14)
- ✓ Starts Grafana container (grafana:9.5.0)
- ✓ Applies all database migrations
- ✓ Configures PostgreSQL datasource with correct UID
- ✓ Provisions both dashboards
- ✓ Connects containers together

## Prerequisites

- Podman installed and running (`podman machine start`)
- PostgreSQL running locally on port 5432
- Database `sast-ai` with MLOps tables created

## Troubleshooting

**Grafana won't start:**
```bash
# Check if podman machine is running
podman machine list

# Start podman machine if needed
podman machine start
```

**No data showing in dashboards:**
```bash
# Check PostgreSQL is running
psql -h localhost -p 5432 -U quarkus -d sast-ai -c "SELECT COUNT(*) FROM mlops_batch;"

# Check if tables exist
psql -h localhost -p 5432 -U quarkus -d sast-ai -c "\dt mlops*"
```

**Dashboard UID errors:**
- The datasource is automatically configured with UID `P413637974B2AAB20` to match the dashboards
- This is set in `grafana/datasources/postgresql.yml`

## Configuration Files

- `Makefile.grafana` - Start/stop commands
- `grafana/datasources/postgresql.yml` - PostgreSQL connection config
- `grafana/provisioning/dashboards.yml` - Dashboard provisioning config
- `grafana/dashboards/*.json` - Dashboard definitions
