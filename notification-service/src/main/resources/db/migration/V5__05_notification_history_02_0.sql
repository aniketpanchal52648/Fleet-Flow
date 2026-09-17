CREATE INDEX IF NOT EXISTS idx_notif_recipient ON notification_service.notification_history (recipient_id, recipient_type);

CREATE INDEX IF NOT EXISTS idx_notif_status_created ON notification_service.notification_history (status, created_at);
