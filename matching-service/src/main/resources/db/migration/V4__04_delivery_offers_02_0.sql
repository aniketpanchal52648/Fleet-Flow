CREATE INDEX IF NOT EXISTS idx_offer_shipment ON matching_service.delivery_offers (shipment_id);
CREATE INDEX IF NOT EXISTS idx_offer_driver ON matching_service.delivery_offers (driver_id);
CREATE INDEX IF NOT EXISTS idx_offer_status ON matching_service.delivery_offers (status);

-- FCFS Concurrency Control: Guarantees at the database engine level that no two offers for the same shipment can ever have status = 'ACCEPTED'
CREATE UNIQUE INDEX IF NOT EXISTS uq_shipment_accepted_offer 
ON matching_service.delivery_offers (shipment_id) 
WHERE (status = 'ACCEPTED');
