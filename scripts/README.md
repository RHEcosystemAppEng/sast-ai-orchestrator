# MLOps Mock Data Generator

This directory contains scripts for generating mock data for the MLOps batch metrics system.

## generate_mock_data.py

This script generates mock data for the `mlops_batch`, `mlops_job`, and `mlops_job_metrics` tables based on a CSV file containing cumulative results.

### Prerequisites

1. PostgreSQL database with the MLOps schema (migrations applied)
2. Python 3.x with `psycopg2` installed:
   ```bash
   pip install psycopg2-binary
   ```

### Usage

Basic usage with default database connection (localhost:5432):

```bash
python scripts/generate_mock_data.py "/path/to/Results - Cumulative Results.csv"
```

### Command Line Options

```bash
python scripts/generate_mock_data.py --help
```

Options:
- `csv_file` - Path to the cumulative results CSV file (required)
- `--clean` - Clean existing mock data before inserting new data
- `--db-host` - Database host (default: localhost)
- `--db-port` - Database port (default: 5432)
- `--db-name` - Database name (default: mlops)
- `--db-user` - Database user (default: postgres)
- `--db-password` - Database password (default: postgres)

### Examples

1. Generate mock data with custom database connection:
   ```bash
   python scripts/generate_mock_data.py \
     "/Users/gziv/Downloads/Results - Cumulative Results.csv" \
     --db-host localhost \
     --db-port 5432 \
     --db-name mlops \
     --db-user myuser \
     --db-password mypassword
   ```

2. Clean existing mock data and insert new data:
   ```bash
   python scripts/generate_mock_data.py \
     "/Users/gziv/Downloads/Results - Cumulative Results.csv" \
     --clean
   ```

3. Use environment variables for database connection:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=mlops
   export DB_USER=postgres
   export DB_PASSWORD=postgres

   python scripts/generate_mock_data.py "/path/to/Results - Cumulative Results.csv"
   ```

### CSV File Format

The script expects a CSV file with the following columns:
- `date` - Timestamp in format `YYYY-MM-DD HH:MM:SS`
- `nvr` - Package NVR (Name-Version-Release)
- `tp` - True Positives
- `fp` - False Positives
- `tn` - True Negatives
- `fn` - False Negatives
- `accuracy` - Accuracy score (0-1)
- `recall` - Recall score (0-1)
- `precision` - Precision score (0-1)
- `f1 score` - F1 score (0-1)

Example CSV:
```csv
date,nvr,tp,fp,tn,fn,accuracy,recall,precision,f1 score
2025-11-13 15:40:16,libksba-1.6.5-3,2,0,1,0,1,1,1,1
2025-11-13 15:47:06,gzip-1.13-1,8,1,3,0,0.9166666667,1,0.8888888889,0.9411764706
```

### What the Script Does

1. Parses the CSV file and groups results by date
2. Creates a batch for each unique date in the data
3. For each batch:
   - Creates a `mlops_batch` record with DVC versioning info
   - Creates `mlops_job` records for each package
   - Creates `mlops_job_metrics` records with the confusion matrix and performance metrics
4. All data is marked with `submitted_by = 'mock_user'` for easy cleanup

### Viewing Results in Grafana

After running the script, you can view the results in Grafana using the pre-configured dashboards:

1. **MLOps Batch Metrics Overview** (`/grafana/dashboards/mlops-batch-metrics.json`)
   - Latest batch accuracy, precision, recall, and F1 score gauges
   - Batch performance over time
   - Confusion matrix for recent batches
   - Batch comparison table
   - Performance by prompt version

2. **MLOps Job Details** (`/grafana/dashboards/mlops-job-details.json`)
   - Job-level metrics distribution
   - Detailed job metrics table
   - Job completion timeline

### Cleanup

To remove mock data from the database:

```bash
python scripts/generate_mock_data.py \
  "/path/to/Results - Cumulative Results.csv" \
  --clean
```

Or manually:
```sql
DELETE FROM mlops_job_metrics WHERE mlops_job_id IN (SELECT id FROM mlops_job WHERE submitted_by = 'mock_user');
DELETE FROM mlops_job WHERE submitted_by = 'mock_user';
DELETE FROM mlops_batch WHERE submitted_by = 'mock_user';
```

### Troubleshooting

**Connection errors:**
- Ensure PostgreSQL is running
- Check database credentials
- Verify network connectivity to database host

**Import errors:**
- Install psycopg2: `pip install psycopg2-binary`

**CSV parsing errors:**
- Ensure CSV file has correct header row
- Check date format matches `YYYY-MM-DD HH:MM:SS`
- Verify numeric columns contain valid numbers