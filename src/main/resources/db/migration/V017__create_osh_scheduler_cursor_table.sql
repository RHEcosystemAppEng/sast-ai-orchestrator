-- Migration to create OSH scheduler cursor tracking table
-- Version: V017 - Add osh_scheduler_cursor table for incremental polling

-- Create osh_scheduler_cursor table for tracking OSH polling position
CREATE TABLE IF NOT EXISTS osh_scheduler_cursor (
    id BIGSERIAL PRIMARY KEY,
    last_seen_token VARCHAR(255),
    last_seen_timestamp TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
);

-- Create index for efficient cursor queries
CREATE INDEX IF NOT EXISTS idx_osh_scheduler_cursor_id ON osh_scheduler_cursor(id);

-- Comments for documentation
COMMENT ON TABLE osh_scheduler_cursor IS 'Tracks the last processed scan ID for OSH incremental polling';
COMMENT ON COLUMN osh_scheduler_cursor.last_seen_token IS 'Last OSH scan ID that was processed (stored as string for flexibility)';
COMMENT ON COLUMN osh_scheduler_cursor.last_seen_timestamp IS 'Timestamp when the last scan was seen';
COMMENT ON COLUMN osh_scheduler_cursor.updated_at IS 'Last update timestamp for cursor position';