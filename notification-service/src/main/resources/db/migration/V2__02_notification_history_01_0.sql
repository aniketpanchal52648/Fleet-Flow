CREATE SCHEMA IF NOT EXISTS notification_service;

CREATE TABLE IF NOT EXISTS notification_service.notification_history (
    id VARCHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(50) NOT NULL,
    recipient_type VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(50),
    title VARCHAR(255),
    body TEXT,
    payload TEXT,
    status VARCHAR(30) NOT NULL,
    error_message TEXT,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS recipient_id VARCHAR(50);
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS recipient_type VARCHAR(20);
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS channel VARCHAR(20);
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS event_type VARCHAR(50);
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS reference_id VARCHAR(50);
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS title VARCHAR(255);
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS body TEXT;
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS payload TEXT;
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS status VARCHAR(30);
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS error_message TEXT;
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS sent_at TIMESTAMP;
ALTER TABLE notification_service.notification_history ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
