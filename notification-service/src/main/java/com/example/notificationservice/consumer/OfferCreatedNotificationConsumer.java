package com.example.notificationservice.consumer;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.entities.NotificationChannel;
import com.example.notificationservice.entities.ProcessedEvent;
import com.example.notificationservice.entities.RecipientType;
import com.example.notificationservice.repository.ProcessedEventRepository;
import com.example.notificationservice.service.NotificationDispatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.DeliveryOfferCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OfferCreatedNotificationConsumer {

    private final NotificationDispatcherService notificationDispatcherService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.offer-created:delivery-offer.created}",
            groupId = "${spring.kafka.consumer.group-id:notification-service-group}"
    )
    public void consumeOfferCreated(String message) {
        log.info("Received Kafka message on topic [delivery-offer.created]: {}", message);

        try {
            DeliveryOfferCreatedEvent event = objectMapper.readValue(message, DeliveryOfferCreatedEvent.class);

            // Deduplication Check
            String eventId = event.getEventId() != null && !event.getEventId().isBlank()
                    ? event.getEventId()
                    : "OFFER_CREATED_" + event.getOfferId();

            if (processedEventRepository.existsById(eventId)) {
                log.info("DeliveryOfferCreatedEvent [{}] has already been processed. Skipping deduplicated event.", eventId);
                return;
            }

            // Build Notification Payload
            Map<String, String> dataPayload = new HashMap<>();
            dataPayload.put("offerId", event.getOfferId() != null ? event.getOfferId() : "");
            dataPayload.put("shipmentId", event.getShipmentId() != null ? event.getShipmentId() : "");
            dataPayload.put("driverId", event.getDriverId() != null ? event.getDriverId() : "");
            dataPayload.put("vehicleId", event.getVehicleId() != null ? event.getVehicleId() : "");
            dataPayload.put("offeredPrice", event.getOfferedPrice() != null ? String.valueOf(event.getOfferedPrice()) : "0.0");
            dataPayload.put("expiresAt", event.getExpiresAt() != null ? event.getExpiresAt().toString() : "");

            NotificationRequest request = NotificationRequest.builder()
                    .recipientId(event.getDriverId())
                    .recipientType(RecipientType.DRIVER)
                    .channel(NotificationChannel.PUSH)
                    .eventType("DELIVERY_OFFER_CREATED")
                    .referenceId(event.getOfferId())
                    .title("New Delivery Offer Available!")
                    .body(String.format("You received an offer for shipment %s. Payout: $%.2f. Tap to review.",
                            event.getShipmentId(),
                            event.getOfferedPrice() != null ? event.getOfferedPrice() : 0.0))
                    .data(dataPayload)
                    .build();

            // Dispatch notification (handles token resolution, mock/firebase provider, and history recording)
            notificationDispatcherService.dispatch(request);

            // Mark event as processed
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(eventId)
                    .build());

            log.info("Successfully processed DeliveryOfferCreatedEvent [{}] for offer [{}] and driver [{}]",
                    eventId, event.getOfferId(), event.getDriverId());

        } catch (Exception e) {
            log.error("Failed to process DeliveryOfferCreatedEvent from message [{}]: {}", message, e.getMessage(), e);
        }
    }
}
