-- Create ground_truth table for storing manual Ground Truth data from Excel files
CREATE TABLE IF NOT EXISTS ground_truth (
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

CREATE INDEX IF NOT EXISTS idx_ground_truth_package_nvr ON ground_truth(package_nvr);
CREATE INDEX IF NOT EXISTS idx_ground_truth_package_issue ON ground_truth(package_nvr, issue_number);