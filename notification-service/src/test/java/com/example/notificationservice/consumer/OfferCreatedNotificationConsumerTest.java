package com.example.notificationservice.consumer;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.entities.NotificationChannel;
import com.example.notificationservice.entities.ProcessedEvent;
import com.example.notificationservice.entities.RecipientType;
import com.example.notificationservice.repository.ProcessedEventRepository;
import com.example.notificationservice.service.NotificationDispatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.event.DeliveryOfferCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferCreatedNotificationConsumerTest {

    @Mock
    private NotificationDispatcherService notificationDispatcherService;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private ObjectMapper objectMapper;
    private OfferCreatedNotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        consumer = new OfferCreatedNotificationConsumer(
                notificationDispatcherService,
                processedEventRepository,
                objectMapper
        );
    }

    @Test
    @DisplayName("Should consume DeliveryOfferCreatedEvent, dispatch push notification, and record processed event")
    void testConsumeOfferCreatedSuccess() throws Exception {
        DeliveryOfferCreatedEvent event = DeliveryOfferCreatedEvent.builder()
                .eventId("evt-offer-101")
                .eventType("DELIVERY_OFFER_CREATED")
                .offerId("offer-101")
                .shipmentId("ship-555")
                .driverId("driver-42")
                .vehicleId("veh-99")
                .offeredPrice(150.0)
                .expiresAt(LocalDateTime.now().plusSeconds(60))
                .createdAt(LocalDateTime.now())
                .build();

        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-offer-101")).thenReturn(false);

        consumer.consumeOfferCreated(json);

        ArgumentCaptor<NotificationRequest> reqCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationDispatcherService, times(1)).dispatch(reqCaptor.capture());

        NotificationRequest captured = reqCaptor.getValue();
        assertEquals("driver-42", captured.getRecipientId());
        assertEquals(RecipientType.DRIVER, captured.getRecipientType());
        assertEquals(NotificationChannel.PUSH, captured.getChannel());
        assertEquals("DELIVERY_OFFER_CREATED", captured.getEventType());
        assertEquals("offer-101", captured.getReferenceId());
        assertTrue(captured.getBody().contains("ship-555"));

        ArgumentCaptor<ProcessedEvent> eventCaptor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository, times(1)).save(eventCaptor.capture());
        assertEquals("evt-offer-101", eventCaptor.getValue().getEventId());
    }

    @Test
    @DisplayName("Should skip processing when event has already been processed (idempotency deduplication)")
    void testConsumeDuplicateEventSkipped() throws Exception {
        DeliveryOfferCreatedEvent event = DeliveryOfferCreatedEvent.builder()
                .eventId("evt-offer-duplicate")
                .eventType("DELIVERY_OFFER_CREATED")
                .offerId("offer-102")
                .shipmentId("ship-556")
                .driverId("driver-42")
                .build();

        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-offer-duplicate")).thenReturn(true);

        consumer.consumeOfferCreated(json);

        verify(notificationDispatcherService, never()).dispatch(any());
        verify(processedEventRepository, never()).save(any());
    }
}
