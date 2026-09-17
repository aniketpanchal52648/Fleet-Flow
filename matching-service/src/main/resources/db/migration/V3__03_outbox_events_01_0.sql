CREATE SCHEMA IF NOT EXISTS matching_service;

CREATE TABLE IF NOT EXISTS matching_service.outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(50),
    aggregate_id VARCHAR(50),
    event_type VARCHAR(50),
    payload TEXT,
    status VARCHAR(20),
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP,
    processed_at TIMESTAMP
);

ALTER TABLE matching_service.outbox_events ADD COLUMN IF NOT EXISTS aggregate_type VARCHAR(50);
ALTER TABLE matching_service.outbox_events ADD COLUMN IF NOT EXISTS aggregate_id VARCHAR(50);
ALTER TABLE matching_service.outbox_events ADD COLUMN IF NOT EXISTS event_type VARCHAR(50);
ALTER TABLE matching_service.outbox_events ADD COLUMN IF NOT EXISTS payload TEXT;
ALTER TABLE matching_service.outbox_events ADD COLUMN IF NOT EXISTS status VARCHAR(20);
ALTER TABLE matching_service.outbox_events ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
ALTER TABLE matching_service.outbox_events ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE matching_service.outbox_events ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP;
