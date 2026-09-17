CREATE SCHEMA IF NOT EXISTS notification_service;

CREATE TABLE IF NOT EXISTS notification_service.device_tokens (
    id VARCHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(50) NOT NULL,
    recipient_type VARCHAR(20) NOT NULL,
    device_token VARCHAR(500) NOT NULL,
    device_type VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

ALTER TABLE notification_service.device_tokens ADD COLUMN IF NOT EXISTS recipient_id VARCHAR(50);
ALTER TABLE notification_service.device_tokens ADD COLUMN IF NOT EXISTS recipient_type VARCHAR(20);
ALTER TABLE notification_service.device_tokens ADD COLUMN IF NOT EXISTS device_token VARCHAR(500);
ALTER TABLE notification_service.device_tokens ADD COLUMN IF NOT EXISTS device_type VARCHAR(20);
ALTER TABLE notification_service.device_tokens ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE notification_service.device_tokens ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE notification_service.device_tokens ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
