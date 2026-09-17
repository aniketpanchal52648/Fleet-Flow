CREATE SCHEMA IF NOT EXISTS matching_service;

CREATE TABLE IF NOT EXISTS matching_service.processed_events (
    event_id VARCHAR(255) PRIMARY KEY,
    processed_at TIMESTAMP
);

ALTER TABLE matching_service.processed_events ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP;
