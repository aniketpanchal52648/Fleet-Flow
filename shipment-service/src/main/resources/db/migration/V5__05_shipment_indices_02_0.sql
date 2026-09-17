CREATE SCHEMA IF NOT EXISTS shipment_service;

CREATE UNIQUE INDEX IF NOT EXISTS uq_shipment_id ON shipment_service.user_shipment(shipment_id);
CREATE INDEX IF NOT EXISTS idx_shipment_user_id ON shipment_service.user_shipment(user_id);
CREATE INDEX IF NOT EXISTS idx_shipment_status ON shipment_service.user_shipment(shipment_status);
CREATE INDEX IF NOT EXISTS idx_shipment_driver_id ON shipment_service.user_shipment(driver_id);
CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON shipment_service.outbox_events(status, created_at);
