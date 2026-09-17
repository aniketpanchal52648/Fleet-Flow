CREATE SCHEMA IF NOT EXISTS shipment_service;

CREATE TABLE IF NOT EXISTS shipment_service.outbox_events (
    id VARCHAR(255) PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    processed_at TIMESTAMP
);
