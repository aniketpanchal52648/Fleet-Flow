package com.example.matchingservice.consumer;

import com.example.matchingservice.entities.ProcessedEvent;
import com.example.matchingservice.repository.ProcessedEventRepository;
import com.example.matchingservice.service.MatchingEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.ShipmentCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventConsumer {

    private final MatchingEngineService matchingEngineService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.shipment-created:shipment-request.created}",
            groupId = "${spring.kafka.consumer.group-id:matching-service-group}"
    )
    public void consumeShipmentCreated(String message) {
        log.info("Received Kafka message on shipment-request.created: {}", message);
        try {
            ShipmentCreatedEvent event = objectMapper.readValue(message, ShipmentCreatedEvent.class);
            String eventId = event.getEventId();

            // 1. Idempotency Check (Deduplication)
            if (eventId != null && processedEventRepository.existsById(eventId)) {
                log.warn("Idempotency safeguard: Event [{}] has already been processed. Skipping duplicate.", eventId);
                return;
            }

            // 2. Process Matching Logic
            matchingEngineService.processShipmentCreated(event);

            // 3. Mark Event as Processed
            if (eventId != null) {
                processedEventRepository.save(ProcessedEvent.builder()
                        .eventId(eventId)
                        .build());
                log.info("Successfully marked event [{}] as processed.", eventId);
            }

        } catch (Exception e) {
            log.error("Error deserializing or processing ShipmentCreatedEvent: {}", message, e);
        }
    }
}
