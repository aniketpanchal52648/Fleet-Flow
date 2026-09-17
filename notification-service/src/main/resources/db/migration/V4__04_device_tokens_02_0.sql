CREATE INDEX IF NOT EXISTS idx_device_recipient ON notification_service.device_tokens (recipient_id, recipient_type);

CREATE UNIQUE INDEX IF NOT EXISTS uq_device_token ON notification_service.device_tokens (device_token);
