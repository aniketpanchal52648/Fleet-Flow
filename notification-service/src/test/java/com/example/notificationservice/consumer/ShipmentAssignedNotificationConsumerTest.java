package com.example.notificationservice.consumer;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.entities.NotificationChannel;
import com.example.notificationservice.entities.ProcessedEvent;
import com.example.notificationservice.entities.RecipientType;
import com.example.notificationservice.repository.ProcessedEventRepository;
import com.example.notificationservice.service.NotificationDispatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.event.ShipmentAssignedEvent;
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
class ShipmentAssignedNotificationConsumerTest {

    @Mock
    private NotificationDispatcherService notificationDispatcherService;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private ObjectMapper objectMapper;
    private ShipmentAssignedNotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        consumer = new ShipmentAssignedNotificationConsumer(
                notificationDispatcherService,
                processedEventRepository,
                objectMapper
        );
    }

    @Test
    @DisplayName("Should consume ShipmentAssignedEvent, dispatch customer push notification, and save processed event")
    void testConsumeShipmentAssignedSuccess() throws Exception {
        ShipmentAssignedEvent event = ShipmentAssignedEvent.builder()
                .eventId("evt-assigned-101")
                .eventType("SHIPMENT_ASSIGNED")
                .shipmentId("SHP-888")
                .userId("USR-CUST-99")
                .driverId("DRV-55")
                .vehicleId("VEH-12")
                .assignedAt(LocalDateTime.now())
                .build();

        when(processedEventRepository.existsById("evt-assigned-101")).thenReturn(false);

        consumer.consumeShipmentAssigned(objectMapper.writeValueAsString(event));

        ArgumentCaptor<NotificationRequest> reqCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationDispatcherService).dispatch(reqCaptor.capture());

        NotificationRequest req = reqCaptor.getValue();
        assertEquals("USR-CUST-99", req.getRecipientId());
        assertEquals(RecipientType.CUSTOMER, req.getRecipientType());
        assertEquals(NotificationChannel.PUSH, req.getChannel());
        assertEquals("SHIPMENT_ASSIGNED", req.getEventType());
        assertEquals("SHP-888", req.getReferenceId());
        assertTrue(req.getBody().contains("SHP-888"));

        ArgumentCaptor<ProcessedEvent> eventCaptor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(eventCaptor.capture());
        assertEquals("evt-assigned-101", eventCaptor.getValue().getEventId());
    }

    @Test
    @DisplayName("Should skip duplicate ShipmentAssignedEvent")
    void testConsumeDuplicateSkipped() throws Exception {
        ShipmentAssignedEvent event = ShipmentAssignedEvent.builder()
                .eventId("evt-duplicate-assign")
                .eventType("SHIPMENT_ASSIGNED")
                .shipmentId("SHP-888")
                .userId("USR-CUST-99")
                .build();

        when(processedEventRepository.existsById("evt-duplicate-assign")).thenReturn(true);

        consumer.consumeShipmentAssigned(objectMapper.writeValueAsString(event));

        verify(notificationDispatcherService, never()).dispatch(any());
        verify(processedEventRepository, never()).save(any());
    }
}
