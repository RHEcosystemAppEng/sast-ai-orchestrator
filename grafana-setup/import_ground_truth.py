#!/usr/bin/env python3
"""
Import Ground Truth data from Excel files into PostgreSQL database.
Only imports the "False Positive?" column as Ground Truth.
"""
import os
import sys
import pandas as pd
import psycopg2
from pathlib import Path

# Database connection parameters
DB_PARAMS = {
    'host': 'localhost',
    'port': 5432,
    'database': 'sast-ai',
    'user': 'quarkus',
    'password': 'quarkus'
}

# Path to extracted reports
REPORTS_DIR = '/tmp/reports'

def create_ground_truth_table(conn):
    """Create the ground_truth table if it doesn't exist."""
    with conn.cursor() as cur:
        cur.execute("""
            DROP TABLE IF EXISTS ground_truth;
            CREATE TABLE ground_truth (
                id SERIAL PRIMARY KEY,
                package_nvr VARCHAR(255) NOT NULL,
                issue_number INTEGER NOT NULL,
                finding TEXT NOT NULL,
                is_false_positive BOOLEAN NOT NULL,
                ai_prediction TEXT,
                comment TEXT,
                hint TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(package_nvr, issue_number)
            );
        """)
        conn.commit()
        print("✓ Created ground_truth table")

def import_excel_file(conn, file_path, package_nvr):
    """Import Ground Truth data from an Excel file."""
    df = pd.read_excel(file_path)

    with conn.cursor() as cur:
        for idx, row in df.iterrows():
            # Extract Ground Truth (yes/y -> True, no/n -> False)
            fp_value = str(row['False Positive?']).strip().lower()
            is_false_positive = fp_value in ('yes', 'y')

            finding = str(row['Finding']) if pd.notna(row['Finding']) else ''

            # Map AI prediction: "True Positive" -> "Issue", "False Positive" -> "Non-Issue"
            ai_pred_raw = str(row['AI prediction']).strip() if pd.notna(row['AI prediction']) else None
            if ai_pred_raw:
                if 'true positive' in ai_pred_raw.lower():
                    ai_prediction = 'Issue'
                elif 'false positive' in ai_pred_raw.lower():
                    ai_prediction = 'Non-Issue'
                else:
                    ai_prediction = ai_pred_raw
            else:
                ai_prediction = None

            comment = str(row['Comment']) if pd.notna(row['Comment']) else None
            hint = str(row['Hint']) if pd.notna(row['Hint']) else None
            issue_number = idx + 1

            try:
                cur.execute("""
                    INSERT INTO ground_truth (package_nvr, issue_number, finding, is_false_positive, ai_prediction, comment, hint)
                    VALUES (%s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (package_nvr, issue_number) DO UPDATE SET
                        finding = EXCLUDED.finding,
                        is_false_positive = EXCLUDED.is_false_positive,
                        ai_prediction = EXCLUDED.ai_prediction,
                        comment = EXCLUDED.comment,
                        hint = EXCLUDED.hint;
                """, (package_nvr, issue_number, finding, is_false_positive, ai_prediction, comment, hint))
            except Exception as e:
                print(f"  ⚠️  Error importing row {issue_number}: {e}")

        conn.commit()

    return len(df)

def main():
    # Connect to database
    try:
        conn = psycopg2.connect(**DB_PARAMS)
        print(f"✓ Connected to PostgreSQL database")
    except Exception as e:
        print(f"✗ Failed to connect to database: {e}")
        sys.exit(1)

    try:
        # Create table
        create_ground_truth_table(conn)

        # Import all Excel files
        reports_path = Path(REPORTS_DIR)
        excel_files = list(reports_path.glob('Demo_input_file_*.xlsx'))

        if not excel_files:
            print(f"✗ No Excel files found in {REPORTS_DIR}")
            sys.exit(1)

        total_rows = 0
        for file_path in sorted(excel_files):
            # Extract package NVR from filename
            package_nvr = file_path.stem.replace('Demo_input_file_', '')

            print(f"Importing {package_nvr}...", end=' ')
            row_count = import_excel_file(conn, file_path, package_nvr)
            total_rows += row_count
            print(f"✓ {row_count} issues")

        print(f"\n✓ Imported {total_rows} total Ground Truth issues from {len(excel_files)} packages")

    except Exception as e:
        print(f"\n✗ Error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
    finally:
        conn.close()

if __name__ == '__main__':
    main()