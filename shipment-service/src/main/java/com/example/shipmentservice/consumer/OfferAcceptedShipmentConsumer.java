package com.example.shipmentservice.consumer;

import com.example.shipmentservice.entities.OutboxEvent;
import com.example.shipmentservice.entities.OutboxStatus;
import com.example.shipmentservice.entities.ProcessedEvent;
import com.example.shipmentservice.entities.Shipment;
import com.example.shipmentservice.entities.ShipmentStatus;
import com.example.shipmentservice.repository.OutboxEventRepository;
import com.example.shipmentservice.repository.ProcessedEventRepository;
import com.example.shipmentservice.repository.ShipmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.DeliveryOfferAcceptedEvent;
import org.example.event.ShipmentAssignedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OfferAcceptedShipmentConsumer {

    private final ShipmentRepository shipmentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.offer-accepted:delivery-offer.accepted}",
            groupId = "${spring.kafka.consumer.group-id:shipment-service-group}"
    )
    @Transactional
    public void consumeOfferAccepted(String message) {
        log.info("Received Kafka message on topic [delivery-offer.accepted]: {}", message);

        try {
            DeliveryOfferAcceptedEvent event = objectMapper.readValue(message, DeliveryOfferAcceptedEvent.class);

            // 1. Consumer Deduplication
            String eventId = event.getEventId() != null && !event.getEventId().isBlank()
                    ? event.getEventId()
                    : "OFFER_ACCEPTED_" + event.getOfferId();

            if (processedEventRepository.existsById(eventId)) {
                log.info("DeliveryOfferAcceptedEvent [{}] already processed. Skipping.", eventId);
                return;
            }

            // 2. Lookup Shipment
            Shipment shipment = shipmentRepository.findByShipmentId(event.getShipmentId()).orElse(null);
            if (shipment == null) {
                log.error("Shipment [{}] not found for accepted offer [{}]. Skipping.", event.getShipmentId(), event.getOfferId());
                return;
            }

            // 3. Update Shipment Assignment Details
            shipment.setDriverId(event.getDriverId());
            shipment.setVehicleId(event.getVehicleId());
            shipment.setShipmentStatus(ShipmentStatus.DRIVER_ASSIGNED);
            shipmentRepository.save(shipment);
            log.info("Updated shipment [{}] status to DRIVER_ASSIGNED with driver [{}]",
                    shipment.getShipmentId(), event.getDriverId());

            // 4. Create ShipmentAssignedEvent and write to Transactional Outbox
            ShipmentAssignedEvent assignedEvent = ShipmentAssignedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("SHIPMENT_ASSIGNED")
                    .shipmentId(shipment.getShipmentId())
                    .userId(shipment.getUserId())
                    .driverId(event.getDriverId())
                    .vehicleId(event.getVehicleId())
                    .assignedAt(LocalDateTime.now())
                    .build();

            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateType("SHIPMENT")
                    .aggregateId(shipment.getShipmentId())
                    .eventType("SHIPMENT_ASSIGNED")
                    .payload(objectMapper.writeValueAsString(assignedEvent))
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(outbox);
            log.info("Saved SHIPMENT_ASSIGNED event in outbox for shipment [{}]", shipment.getShipmentId());

            // 5. Mark event as processed
            processedEventRepository.save(ProcessedEvent.builder().eventId(eventId).build());
            log.info("Successfully processed DeliveryOfferAcceptedEvent [{}] for shipment [{}]", eventId, event.getShipmentId());

        } catch (Exception e) {
            log.error("Failed to process DeliveryOfferAcceptedEvent from message [{}]: {}", message, e.getMessage(), e);
            throw new RuntimeException("Error processing DeliveryOfferAcceptedEvent", e);
        }
    }
}
