package com.example.trackingservice.consumer;

import com.example.trackingservice.service.ShipmentTrackingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.DeliveryOfferAcceptedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryOfferAcceptedConsumer {

    private final ShipmentTrackingService shipmentTrackingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.offer-accepted:delivery-offer.accepted}",
            groupId = "${spring.kafka.consumer.group-id:tracking-service-group}"
    )
    public void consumeOfferAccepted(String message) {
        log.info("Received Kafka message on topic [delivery-offer.accepted]: {}", message);

        try {
            DeliveryOfferAcceptedEvent event = objectMapper.readValue(message, DeliveryOfferAcceptedEvent.class);
            log.info("Processing DeliveryOfferAcceptedEvent: offerId={}, shipmentId={}, driverId={}",
                    event.getOfferId(), event.getShipmentId(), event.getDriverId());

            shipmentTrackingService.assignDriverToShipment(
                    event.getShipmentId(),
                    event.getDriverId(),
                    event.getVehicleId()
            );

            log.info("Successfully updated tracking state for shipment [{}] and driver [{}]",
                    event.getShipmentId(), event.getDriverId());

        } catch (Exception e) {
            log.error("Failed to process DeliveryOfferAcceptedEvent from message [{}]: {}", message, e.getMessage(), e);
        }
    }
}
