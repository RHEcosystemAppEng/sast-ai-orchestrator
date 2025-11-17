#!/usr/bin/env python3
"""
Script to load issue-level details from Excel files into mlops_issue_details table.
Reads Excel files from batch directories and populates the database.
"""

import psycopg2
from psycopg2.extras import execute_values
import pandas as pd
import os
import sys
from pathlib import Path
from datetime import datetime


# Database connection configuration
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


def extract_package_name(filename):
    """Extract package name from filename (remove timestamp suffix)."""
    # Format: package-name-X_YY-ZZ-AA.VV-RR
    # We want just the package name before the timestamp
    parts = filename.split('_')
    if len(parts) >= 2:
        # Take everything before the first timestamp component
        return parts[0].rsplit('-', 3)[0]
    return filename


def load_excel_file(filepath):
    """Load issue data from an Excel file."""
    try:
        # Read the 'AI report' sheet
        df = pd.read_excel(filepath, sheet_name='AI report')

        issues = []
        for _, row in df.iterrows():
            issues.append({
                'issue_id': str(row.get('Issue ID', '')),
                'issue_name': str(row.get('Issue Name', '')),
                'error_description': str(row.get('Error', '')),
                'investigation_result': str(row.get('Investigation Result', '')).strip(),
                'hint': str(row.get('Hint', '')) if pd.notna(row.get('Hint')) else None,
                'justifications': str(row.get('Justifications', '')) if pd.notna(row.get('Justifications')) else None,
                'recommendations': str(row.get('Recommendations', '')) if pd.notna(row.get('Recommendations')) else None,
                'answer_relevancy': parse_relevancy(row.get('Answer Relevancy')),
                'context': str(row.get('Context', '')) if pd.notna(row.get('Context')) else None
            })

        return issues
    except Exception as e:
        print(f"Error loading {filepath}: {e}")
        return []


def parse_relevancy(value):
    """Parse answer relevancy from percentage string or float."""
    if pd.isna(value):
        return None

    if isinstance(value, str):
        # Remove '%' and convert to decimal
        value = value.replace('%', '').strip()
        try:
            return float(value) / 100.0
        except:
            return None
    elif isinstance(value, (int, float)):
        # If already a number, check if it's 0-1 or 0-100
        if value <= 1.0:
            return float(value)
        else:
            return float(value) / 100.0
    return None


def get_job_id_for_package(cursor, batch_id, package_name):
    """Get the mlops_job.id for a given batch and package."""
    cursor.execute("""
        SELECT id FROM mlops_job
        WHERE mlops_batch_id = %s AND package_name = %s
        LIMIT 1
    """, (batch_id, package_name))

    result = cursor.fetchone()
    return result[0] if result else None


def load_batch_directory(batch_dir, batch_id=6):
    """Load all Excel files from a batch directory."""
    conn = connect_db()
    cursor = conn.cursor()

    try:
        total_issues = 0
        files_processed = 0

        # Get all Excel files (they don't have .xlsx extension)
        batch_path = Path(batch_dir)
        excel_files = [f for f in batch_path.iterdir() if f.is_file() and not f.name.startswith('.')]

        print(f"\nProcessing batch directory: {batch_dir}")
        print(f"Found {len(excel_files)} files")

        for excel_file in excel_files:
            package_name = extract_package_name(excel_file.name)
            print(f"\n  Processing: {excel_file.name} -> package: {package_name}")

            # Get the job ID for this package
            job_id = get_job_id_for_package(cursor, batch_id, package_name)

            if not job_id:
                print(f"    WARNING: No job found for package '{package_name}' in batch {batch_id}, skipping")
                continue

            # Load issues from Excel file
            issues = load_excel_file(excel_file)

            if not issues:
                print(f"    No issues found in {excel_file.name}")
                continue

            # Insert issues
            issue_values = []
            for issue in issues:
                issue_values.append((
                    job_id,
                    issue['issue_id'],
                    issue['issue_name'],
                    issue['error_description'],
                    issue['investigation_result'],
                    issue['hint'],
                    issue['justifications'],
                    issue['recommendations'],
                    issue['answer_relevancy'],
                    issue['context'],
                    datetime.now()
                ))

            execute_values(cursor, """
                INSERT INTO mlops_issue_details (
                    mlops_job_id, issue_id, issue_name, error_description,
                    investigation_result, hint, justifications, recommendations,
                    answer_relevancy, context, created_at
                ) VALUES %s
            """, issue_values)

            print(f"    Inserted {len(issues)} issues for job {job_id}")
            total_issues += len(issues)
            files_processed += 1

        conn.commit()
        print(f"\n✓ Successfully loaded {total_issues} issues from {files_processed} files")

    except Exception as e:
        conn.rollback()
        print(f"\n✗ Error loading batch data: {e}")
        import traceback
        traceback.print_exc()
        raise
    finally:
        cursor.close()
        conn.close()


def main():
    """Main entry point."""
    # Default batch directory
    default_batch_dir = '/Users/gziv/Downloads/batch_2025_11_13/2025-11-13'

    batch_dir = sys.argv[1] if len(sys.argv) > 1 else default_batch_dir
    batch_id = int(sys.argv[2]) if len(sys.argv) > 2 else 6

    if not os.path.exists(batch_dir):
        print(f"Error: Batch directory not found: {batch_dir}")
        print(f"\nUsage: {sys.argv[0]} [batch_directory] [batch_id]")
        print(f"Example: {sys.argv[0]} /Users/gziv/Downloads/batch_2025_11_13/2025-11-13 6")
        sys.exit(1)

    print("="*80)
    print("Loading Issue-Level Details into Database")
    print("="*80)
    print(f"Batch Directory: {batch_dir}")
    print(f"Batch ID: {batch_id}")

    load_batch_directory(batch_dir, batch_id)

    print("\n" + "="*80)
    print("Done!")
    print("="*80)


if __name__ == '__main__':
    main()
