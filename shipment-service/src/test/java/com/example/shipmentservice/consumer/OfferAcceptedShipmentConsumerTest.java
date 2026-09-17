package com.example.shipmentservice.consumer;

import com.example.shipmentservice.entities.OutboxEvent;
import com.example.shipmentservice.entities.ProcessedEvent;
import com.example.shipmentservice.entities.Shipment;
import com.example.shipmentservice.entities.ShipmentStatus;
import com.example.shipmentservice.repository.OutboxEventRepository;
import com.example.shipmentservice.repository.ProcessedEventRepository;
import com.example.shipmentservice.repository.ShipmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.event.DeliveryOfferAcceptedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferAcceptedShipmentConsumerTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private ObjectMapper objectMapper;
    private OfferAcceptedShipmentConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        consumer = new OfferAcceptedShipmentConsumer(
                shipmentRepository,
                processedEventRepository,
                outboxEventRepository,
                objectMapper
        );
    }

    @Test
    @DisplayName("Should consume DeliveryOfferAcceptedEvent, update shipment to DRIVER_ASSIGNED, and write SHIPMENT_ASSIGNED outbox")
    void testConsumeOfferAcceptedSuccess() throws Exception {
        DeliveryOfferAcceptedEvent event = DeliveryOfferAcceptedEvent.builder()
                .eventId("evt-accept-101")
                .eventType("DELIVERY_OFFER_ACCEPTED")
                .offerId("OFR-101")
                .shipmentId("SHP-500")
                .driverId("DRV-88")
                .vehicleId("VEH-44")
                .agreedPrice(200.0)
                .acceptedAt(LocalDateTime.now())
                .build();

        Shipment shipment = new Shipment();
        shipment.setShipmentId("SHP-500");
        shipment.setUserId("USR-123");
        shipment.setShipmentStatus(ShipmentStatus.CREATED);

        when(processedEventRepository.existsById("evt-accept-101")).thenReturn(false);
        when(shipmentRepository.findByShipmentId("SHP-500")).thenReturn(Optional.of(shipment));

        consumer.consumeOfferAccepted(objectMapper.writeValueAsString(event));

        // Verify shipment updated
        assertEquals("DRV-88", shipment.getDriverId());
        assertEquals("VEH-44", shipment.getVehicleId());
        assertEquals(ShipmentStatus.DRIVER_ASSIGNED, shipment.getShipmentStatus());
        verify(shipmentRepository).save(shipment);

        // Verify outbox event written
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxCaptor.capture());
        OutboxEvent savedOutbox = outboxCaptor.getValue();
        assertEquals("SHIPMENT", savedOutbox.getAggregateType());
        assertEquals("SHP-500", savedOutbox.getAggregateId());
        assertEquals("SHIPMENT_ASSIGNED", savedOutbox.getEventType());
        assertTrue(savedOutbox.getPayload().contains("USR-123"));

        // Verify processed event saved
        ArgumentCaptor<ProcessedEvent> eventCaptor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(eventCaptor.capture());
        assertEquals("evt-accept-101", eventCaptor.getValue().getEventId());
    }

    @Test
    @DisplayName("Should skip processing when event has already been processed")
    void testConsumeDuplicateEventSkipped() throws Exception {
        DeliveryOfferAcceptedEvent event = DeliveryOfferAcceptedEvent.builder()
                .eventId("evt-duplicate")
                .eventType("DELIVERY_OFFER_ACCEPTED")
                .offerId("OFR-101")
                .shipmentId("SHP-500")
                .build();

        when(processedEventRepository.existsById("evt-duplicate")).thenReturn(true);

        consumer.consumeOfferAccepted(objectMapper.writeValueAsString(event));

        verify(shipmentRepository, never()).findByShipmentId(any());
        verify(shipmentRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }
}
