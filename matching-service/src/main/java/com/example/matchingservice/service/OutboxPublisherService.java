package com.example.matchingservice.service;

import com.example.matchingservice.entities.OutboxEvent;
import com.example.matchingservice.entities.OutboxStatus;
import com.example.matchingservice.repository.OutboxEventRepository;
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

    @Value("${app.kafka.topics.offer-accepted:delivery-offer.accepted}")
    private String offerAcceptedTopic;

    @Value("${app.kafka.topics.offer-created:delivery-offer.created}")
    private String offerCreatedTopic;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            try {
                String targetTopic;
                if ("DELIVERY_OFFER_ACCEPTED".equalsIgnoreCase(event.getEventType())) {
                    targetTopic = offerAcceptedTopic;
                } else if ("DELIVERY_OFFER_CREATED".equalsIgnoreCase(event.getEventType())) {
                    targetTopic = offerCreatedTopic;
                } else {
                    log.warn("Unknown eventType [{}] for outbox event [{}]. Skipping.", event.getEventType(), event.getId());
                    continue;
                }

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
                log.error("Error publishing outbox event [{}]: {}", event.getId(), e.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= 5) {
                    event.setStatus(OutboxStatus.FAILED);
                }
                outboxEventRepository.save(event);
            }
        }
    }
}
