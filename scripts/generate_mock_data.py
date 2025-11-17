#!/usr/bin/env python3
"""
Script to generate mock data for MLOps batch metrics tables based on provided CSV data.
This script populates the mlops_batch, mlops_job, and mlops_job_metrics tables.
"""

import psycopg2
from psycopg2.extras import execute_values
import csv
from datetime import datetime
import os
import sys


# Database connection configuration
# These can be overridden via environment variables
# Defaults match the application.properties settings
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': os.getenv('DB_PORT', '5432'),
    'database': os.getenv('DB_NAME', 'sast-ai'),
    'user': os.getenv('DB_USER', 'quarkus'),
    'password': os.getenv('DB_PASSWORD', 'quarkus')
}


def connect_db():
    """Establish database connection."""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        return conn
    except Exception as e:
        print(f"Error connecting to database: {e}")
        sys.exit(1)


def parse_csv_data(csv_file_path):
    """Parse the cumulative results CSV file."""
    results = []

    with open(csv_file_path, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            results.append({
                'date': datetime.strptime(row['date'], '%Y-%m-%d %H:%M:%S'),
                'nvr': row['nvr'],
                'tp': int(row['tp']),
                'fp': int(row['fp']),
                'tn': int(row['tn']),
                'fn': int(row['fn']),
                'accuracy': float(row['accuracy']),
                'recall': float(row['recall']),
                'precision': float(row['precision']),
                'f1_score': float(row['f1 score'])
            })

    return results


def extract_package_name(nvr):
    """Extract package name from NVR (Name-Version-Release)."""
    # NVR format: name-version-release
    # Example: gzip-1.13-1 -> gzip
    parts = nvr.rsplit('-', 2)
    return parts[0] if parts else nvr


def create_batch_data(results, batch_date='2025-11-13'):
    """Create batch data structure."""
    # Treat all results as a single batch
    # Use the earliest date as the batch date
    if not results:
        return {}

    earliest_date = min(r['date'] for r in results).strftime('%Y-%m-%d')
    return {earliest_date: results}


def insert_mock_data(csv_file_path):
    """Insert mock data into the database."""
    conn = connect_db()
    cursor = conn.cursor()

    try:
        # Parse CSV data
        print(f"Parsing CSV file: {csv_file_path}")
        results = parse_csv_data(csv_file_path)
        print(f"Found {len(results)} package results")

        # Group by date to create batches
        batches = create_batch_data(results)
        print(f"Creating {len(batches)} batches")

        batch_counter = 1
        for batch_date, batch_results in sorted(batches.items()):
            # Create batch entry
            batch_submitted_at = min(r['date'] for r in batch_results)
            batch_last_updated_at = max(r['date'] for r in batch_results)
            total_jobs = len(batch_results)

            cursor.execute("""
                INSERT INTO mlops_batch (
                    testing_data_nvrs_version,
                    prompts_version,
                    known_non_issues_version,
                    container_image,
                    submitted_by,
                    submitted_at,
                    last_updated_at,
                    status,
                    total_jobs,
                    completed_jobs,
                    failed_jobs
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                RETURNING id
            """, (
                f'v1.0.{batch_counter}',  # testing_data_nvrs_version
                f'v2.3.{batch_counter}',  # prompts_version
                f'v1.1.{batch_counter}',  # known_non_issues_version
                f'quay.io/sast-ai/analyzer:v{batch_counter}.0',  # container_image
                'mock_user',  # submitted_by
                batch_submitted_at,  # submitted_at
                batch_last_updated_at,  # last_updated_at
                'completed',  # status
                total_jobs,  # total_jobs
                total_jobs,  # completed_jobs (all completed)
                0  # failed_jobs
            ))

            batch_id = cursor.fetchone()[0]
            print(f"Created batch {batch_id} for date {batch_date} with {total_jobs} jobs")

            # Create job and metrics for each result
            for result in batch_results:
                package_name = extract_package_name(result['nvr'])

                # Insert job
                cursor.execute("""
                    INSERT INTO mlops_job (
                        mlops_batch_id,
                        package_nvr,
                        package_name,
                        project_name,
                        project_version,
                        package_source_code_url,
                        known_false_positives_url,
                        tekton_url,
                        status,
                        created_at,
                        started_at,
                        completed_at,
                        last_updated_at,
                        submitted_by
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    RETURNING id
                """, (
                    batch_id,
                    result['nvr'],
                    package_name,
                    package_name,
                    result['nvr'].replace(package_name + '-', ''),  # version-release
                    f'https://src.fedoraproject.org/rpms/{package_name}',
                    f'https://example.com/fp/{package_name}',
                    f'https://tekton.example.com/runs/{batch_id}-{package_name}',
                    'completed',
                    result['date'],  # created_at
                    result['date'],  # started_at
                    result['date'],  # completed_at
                    result['date'],  # last_updated_at
                    'mock_user'
                ))

                job_id = cursor.fetchone()[0]

                # Insert metrics
                cursor.execute("""
                    INSERT INTO mlops_job_metrics (
                        mlops_job_id,
                        package_name,
                        accuracy,
                        precision,
                        recall,
                        f1_score,
                        cm_tp,
                        cm_fp,
                        cm_tn,
                        cm_fn,
                        created_at
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """, (
                    job_id,
                    package_name,
                    result['accuracy'],
                    result['precision'],
                    result['recall'],
                    result['f1_score'],
                    result['tp'],
                    result['fp'],
                    result['tn'],
                    result['fn'],
                    result['date']
                ))

            batch_counter += 1

        # Commit transaction
        conn.commit()
        print("\nMock data inserted successfully!")
        print(f"Total batches created: {len(batches)}")
        print(f"Total jobs created: {len(results)}")

        # Display summary statistics
        cursor.execute("""
            SELECT
                COUNT(*) as batch_count,
                SUM(total_jobs) as total_jobs,
                SUM(completed_jobs) as completed_jobs
            FROM mlops_batch
        """)
        stats = cursor.fetchone()
        print(f"\nDatabase summary:")
        print(f"  Batches: {stats[0]}")
        print(f"  Total jobs: {stats[1]}")
        print(f"  Completed jobs: {stats[2]}")

    except psycopg2.errors.UndefinedTable as e:
        conn.rollback()
        print("\n" + "="*80)
        print("ERROR: Database tables do not exist!")
        print("="*80)
        print("\nPlease run the Quarkus application first to create the database schema.")
        print("The Flyway migrations will create the required tables.")
        print("\nTo start the application in dev mode:")
        print("  cd /Users/gziv/Dev/sast-ai-orchestrator")
        print("  ./mvnw quarkus:dev")
        print("\nOr build and run:")
        print("  ./mvnw package")
        print("  java -jar target/quarkus-app/quarkus-run.jar")
        print("="*80 + "\n")
        sys.exit(1)
    except Exception as e:
        conn.rollback()
        print(f"Error inserting data: {e}")
        import traceback
        traceback.print_exc()
        raise
    finally:
        cursor.close()
        conn.close()


def clean_existing_data():
    """Clean existing mock data from tables."""
    conn = connect_db()
    cursor = conn.cursor()

    try:
        print("Cleaning existing data...")
        cursor.execute("DELETE FROM mlops_job_metrics WHERE mlops_job_id IN (SELECT id FROM mlops_job WHERE submitted_by = 'mock_user')")
        cursor.execute("DELETE FROM mlops_job WHERE submitted_by = 'mock_user'")
        cursor.execute("DELETE FROM mlops_batch WHERE submitted_by = 'mock_user'")
        conn.commit()
        print("Existing mock data cleaned successfully")
    except psycopg2.errors.UndefinedTable as e:
        conn.rollback()
        print("\n" + "="*80)
        print("ERROR: Database tables do not exist!")
        print("="*80)
        print("\nPlease run the Quarkus application first to create the database schema.")
        print("The Flyway migrations will create the required tables.")
        print("\nTo start the application in dev mode:")
        print("  cd /Users/gziv/Dev/sast-ai-orchestrator")
        print("  ./mvnw quarkus:dev")
        print("\nOr build and run:")
        print("  ./mvnw package")
        print("  java -jar target/quarkus-app/quarkus-run.jar")
        print("="*80 + "\n")
        sys.exit(1)
    except Exception as e:
        conn.rollback()
        print(f"Error cleaning data: {e}")
        raise
    finally:
        cursor.close()
        conn.close()


if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(description='Generate mock data for MLOps batch metrics')
    parser.add_argument('csv_file', help='Path to the cumulative results CSV file')
    parser.add_argument('--clean', action='store_true', help='Clean existing mock data before inserting')
    parser.add_argument('--db-host', help='Database host (default: localhost)')
    parser.add_argument('--db-port', help='Database port (default: 5432)')
    parser.add_argument('--db-name', help='Database name (default: mlops)')
    parser.add_argument('--db-user', help='Database user (default: postgres)')
    parser.add_argument('--db-password', help='Database password (default: postgres)')

    args = parser.parse_args()

    # Override DB config if provided
    if args.db_host:
        DB_CONFIG['host'] = args.db_host
    if args.db_port:
        DB_CONFIG['port'] = args.db_port
    if args.db_name:
        DB_CONFIG['database'] = args.db_name
    if args.db_user:
        DB_CONFIG['user'] = args.db_user
    if args.db_password:
        DB_CONFIG['password'] = args.db_password

    # Clean existing data if requested
    if args.clean:
        clean_existing_data()

    # Insert mock data
    insert_mock_data(args.csv_file)