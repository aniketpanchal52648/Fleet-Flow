CREATE SCHEMA IF NOT EXISTS matching_service;

CREATE TABLE IF NOT EXISTS matching_service.delivery_offers (
    id VARCHAR(36) PRIMARY KEY,
    offer_id VARCHAR(50),
    shipment_id VARCHAR(50),
    driver_id VARCHAR(50),
    vehicle_id VARCHAR(50),
    offered_price DOUBLE PRECISION,
    status VARCHAR(20),
    expires_at TIMESTAMP,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE matching_service.delivery_offers ADD COLUMN IF NOT EXISTS offer_id VARCHAR(50);
ALTER TABLE matching_service.delivery_offers ADD COLUMN IF NOT EXISTS shipment_id VARCHAR(50);
ALTER TABLE matching_service.delivery_offers ADD COLUMN IF NOT EXISTS driver_id VARCHAR(50);
ALTER TABLE matching_service.delivery_offers ADD COLUMN IF NOT EXISTS vehicle_id VARCHAR(50);
ALTER TABLE matching_service.delivery_offers ADD COLUMN IF NOT EXISTS offered_price DOUBLE PRECISION;
ALTER TABLE matching_service.delivery_offers ADD COLUMN IF NOT EXISTS status VARCHAR(20);
ALTER TABLE matching_service.delivery_offers ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE matching_service.delivery_offers ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE matching_service.delivery_offers ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE matching_service.delivery_offers ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint c
        JOIN pg_namespace n ON n.oid = c.connamespace
        WHERE n.nspname = 'matching_service' AND c.conname = 'uq_delivery_offers_offer_id'
    ) THEN
        ALTER TABLE matching_service.delivery_offers 
        ADD CONSTRAINT uq_delivery_offers_offer_id UNIQUE (offer_id);
    END IF;
END $$;
