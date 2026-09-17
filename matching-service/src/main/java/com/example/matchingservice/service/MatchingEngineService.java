package com.example.matchingservice.service;

import com.example.matchingservice.dto.NearbyDriverDto;
import com.example.matchingservice.entities.DeliveryOffer;
import com.example.matchingservice.entities.OfferStatus;
import com.example.matchingservice.entities.OutboxEvent;
import com.example.matchingservice.entities.OutboxStatus;
import com.example.matchingservice.repository.DeliveryOfferRepository;
import com.example.matchingservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.DeliveryOfferCreatedEvent;
import org.example.event.ShipmentCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingEngineService {

    private final TrackingClientService trackingClientService;
    private final DeliveryOfferRepository offerRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.matching.default-radius-km:10.0}")
    private Double defaultRadiusKm;

    @Value("${app.matching.offer-expiry-minutes:5}")
    private int offerExpiryMinutes;

    @Transactional
    public void processShipmentCreated(ShipmentCreatedEvent event) {
        log.info("Processing ShipmentCreatedEvent for shipmentId: {}", event.getShipmentId());

        if (event.getPickupAddress() == null || event.getPickupAddress().getGeoLocation() == null) {
            log.warn("Shipment [{}] has no pickup geoLocation. Skipping automated driver matching.", event.getShipmentId());
            return;
        }

        // 1. Parse coordinates from "lat,lon" string
        String[] parts = event.getPickupAddress().getGeoLocation().split(",");
        if (parts.length != 2) {
            log.error("Invalid geoLocation format [{}] for shipment: {}", event.getPickupAddress().getGeoLocation(), event.getShipmentId());
            return;
        }

        Double latitude;
        Double longitude;
        try {
            latitude = Double.parseDouble(parts[0].trim());
            longitude = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            log.error("Could not parse coordinates from geoLocation: {}", event.getPickupAddress().getGeoLocation());
            return;
        }

        // 2. Discover nearby drivers from Tracking Service (Redis GEO index)
        List<NearbyDriverDto> candidates = trackingClientService.getNearbyDrivers(latitude, longitude, defaultRadiusKm);

        if (candidates.isEmpty()) {
            log.warn("No nearby online drivers found within {} km for shipment: {}", defaultRadiusKm, event.getShipmentId());
            return;
        }

        // 3. Generate OPEN delivery offers for all eligible nearby drivers
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(offerExpiryMinutes);

        for (NearbyDriverDto candidate : candidates) {
            String offerId = "OFR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            // Pricing algorithm: base price $100 + ($15/km)
            Double offeredPrice = Math.round((100.0 + (candidate.getDistanceKm() * 15.0)) * 100.0) / 100.0;

            DeliveryOffer offer = DeliveryOffer.builder()
                    .offerId(offerId)
                    .shipmentId(event.getShipmentId())
                    .driverId(candidate.getDriverId())
                    .vehicleId(candidate.getVehicleId())
                    .offeredPrice(offeredPrice)
                    .status(OfferStatus.OPEN)
                    .expiresAt(expiresAt)
                    .build();

            DeliveryOffer saved = offerRepository.save(offer);
            log.info("Created OPEN delivery offer [{}] for driver [{}] on shipment [{}] ($ {})",
                    saved.getOfferId(), candidate.getDriverId(), event.getShipmentId(), offeredPrice);

            // 4. Save Outbox Event for Notification Service
            saveOfferCreatedOutboxEvent(saved);
        }
    }

    private void saveOfferCreatedOutboxEvent(DeliveryOffer offer) {
        try {
            DeliveryOfferCreatedEvent event = DeliveryOfferCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("DELIVERY_OFFER_CREATED")
                    .offerId(offer.getOfferId())
                    .shipmentId(offer.getShipmentId())
                    .driverId(offer.getDriverId())
                    .vehicleId(offer.getVehicleId())
                    .offeredPrice(offer.getOfferedPrice())
                    .expiresAt(offer.getExpiresAt())
                    .createdAt(LocalDateTime.now())
                    .build();

            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateType("DELIVERY_OFFER")
                    .aggregateId(offer.getOfferId())
                    .eventType("DELIVERY_OFFER_CREATED")
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(outbox);
        } catch (Exception e) {
            log.error("Failed to serialize DeliveryOfferCreatedEvent for offer: {}", offer.getOfferId(), e);
        }
    }
}
