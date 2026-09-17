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
import org.example.event.ShipmentAssignedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentAssignedNotificationConsumer {

    private final NotificationDispatcherService notificationDispatcherService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.shipment-assigned:shipment.assigned}",
            groupId = "${spring.kafka.consumer.group-id:notification-service-group}"
    )
    public void consumeShipmentAssigned(String message) {
        log.info("Received Kafka message on topic [shipment.assigned]: {}", message);

        try {
            ShipmentAssignedEvent event = objectMapper.readValue(message, ShipmentAssignedEvent.class);

            // Deduplication Check
            String eventId = event.getEventId() != null && !event.getEventId().isBlank()
                    ? event.getEventId()
                    : "SHIPMENT_ASSIGNED_" + event.getShipmentId();

            if (processedEventRepository.existsById(eventId)) {
                log.info("ShipmentAssignedEvent [{}] has already been processed. Skipping deduplicated event.", eventId);
                return;
            }

            // Build Notification Payload for Customer
            Map<String, String> dataPayload = new HashMap<>();
            dataPayload.put("shipmentId", event.getShipmentId() != null ? event.getShipmentId() : "");
            dataPayload.put("driverId", event.getDriverId() != null ? event.getDriverId() : "");
            dataPayload.put("vehicleId", event.getVehicleId() != null ? event.getVehicleId() : "");
            dataPayload.put("assignedAt", event.getAssignedAt() != null ? event.getAssignedAt().toString() : "");

            NotificationRequest request = NotificationRequest.builder()
                    .recipientId(event.getUserId())
                    .recipientType(RecipientType.CUSTOMER)
                    .channel(NotificationChannel.PUSH)
                    .eventType("SHIPMENT_ASSIGNED")
                    .referenceId(event.getShipmentId())
                    .title("Driver Assigned!")
                    .body(String.format("A driver has been assigned to your shipment %s. Vehicle: %s. Tap to track live.",
                            event.getShipmentId(),
                            event.getVehicleId() != null ? event.getVehicleId() : "N/A"))
                    .data(dataPayload)
                    .build();

            // Dispatch notification to customer
            notificationDispatcherService.dispatch(request);

            // Mark event as processed
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(eventId)
                    .build());

            log.info("Successfully processed ShipmentAssignedEvent [{}] for shipment [{}] and customer [{}]",
                    eventId, event.getShipmentId(), event.getUserId());

        } catch (Exception e) {
            log.error("Failed to process ShipmentAssignedEvent from message [{}]: {}", message, e.getMessage(), e);
        }
    }
}
