-- Optimized index for OutboxPublisherService polling PENDING events ordered by created_at
CREATE INDEX IF NOT EXISTS idx_outbox_status_created_at ON matching_service.outbox_events (status, created_at);
