package com.example.shipmentservice.service;

import com.example.shipmentservice.entities.OutboxEvent;
import com.example.shipmentservice.entities.OutboxStatus;
import com.example.shipmentservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topics.shipment-created:shipment-request.created}")
    private String shipmentCreatedTopic;

    @Value("${app.kafka.topics.shipment-assigned:shipment.assigned}")
    private String shipmentAssignedTopic;

    @Scheduled(fixedDelay = 2000) // Runs every 2 seconds
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            try {
                String targetTopic;
                if ("SHIPMENT_CREATED".equalsIgnoreCase(event.getEventType())) {
                    targetTopic = shipmentCreatedTopic;
                } else if ("SHIPMENT_ASSIGNED".equalsIgnoreCase(event.getEventType())) {
                    targetTopic = shipmentAssignedTopic;
                } else {
                    log.warn("Unknown eventType [{}] for outbox event [{}]. Skipping.", event.getEventType(), event.getId());
                    continue;
                }

                // Key = aggregateId (shipmentId), Value = JSON payload string
                kafkaTemplate.send(targetTopic, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Successfully published outbox event [{}] of type [{}] to Kafka topic [{}]",
                                        event.getId(), event.getEventType(), targetTopic);
                            } else {
                                log.error("Failed to publish outbox event [{}] to Kafka topic [{}]: {}",
                                        event.getId(), targetTopic, ex.getMessage());
                            }
                        });

                event.setStatus(OutboxStatus.SENT);
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

            } catch (Exception e) {
                log.error("Error processing outbox event [{}]: {}", event.getId(), e.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= 5) {
                    event.setStatus(OutboxStatus.FAILED);
                }
                outboxEventRepository.save(event);
            }
        }
    }
}