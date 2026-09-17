CREATE SCHEMA IF NOT EXISTS shipment_service;

CREATE TABLE IF NOT EXISTS shipment_service.user_shipment (
    id VARCHAR(255) PRIMARY KEY,
    shipment_id VARCHAR(100),
    user_id VARCHAR(100),
    pickup_location_id VARCHAR(255),
    drop_location_id VARCHAR(255),
    shipment_status VARCHAR(50),
    driver_id VARCHAR(100),
    vehicle_id VARCHAR(100),
    vehicle_type VARCHAR(100),
    weight_kg DOUBLE PRECISION,
    cancellation_reason VARCHAR(500),
    pickup_time TIMESTAMP,
    delivery_time TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_shipment_pickup'
    ) THEN
        ALTER TABLE shipment_service.user_shipment
            ADD CONSTRAINT fk_shipment_pickup
            FOREIGN KEY (pickup_location_id) REFERENCES shipment_service.shipment_user_address(id)
            ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_shipment_drop'
    ) THEN
        ALTER TABLE shipment_service.user_shipment
            ADD CONSTRAINT fk_shipment_drop
            FOREIGN KEY (drop_location_id) REFERENCES shipment_service.shipment_user_address(id)
            ON DELETE SET NULL;
    END IF;
END $$;
