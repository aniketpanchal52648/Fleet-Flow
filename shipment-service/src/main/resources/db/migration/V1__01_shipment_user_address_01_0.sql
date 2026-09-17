CREATE SCHEMA IF NOT EXISTS shipment_service;

CREATE TABLE IF NOT EXISTS shipment_service.shipment_user_address (
    id VARCHAR(255) PRIMARY KEY,
    pincode INTEGER NOT NULL,
    state VARCHAR(255),
    user_id VARCHAR(255),
    district VARCHAR(255),
    address_line_one VARCHAR(255),
    address_line_two VARCHAR(255),
    address_type SMALLINT,
    geo_location VARCHAR(255)
);
