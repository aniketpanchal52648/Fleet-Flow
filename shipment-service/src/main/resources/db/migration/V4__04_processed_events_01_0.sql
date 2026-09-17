CREATE SCHEMA IF NOT EXISTS shipment_service;

CREATE TABLE IF NOT EXISTS shipment_service.processed_events (
    event_id VARCHAR(255) PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
