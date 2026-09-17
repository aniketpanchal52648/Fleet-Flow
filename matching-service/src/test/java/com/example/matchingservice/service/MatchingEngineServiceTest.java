package com.example.matchingservice.service;

import com.example.matchingservice.dto.NearbyDriverDto;
import com.example.matchingservice.entities.DeliveryOffer;
import com.example.matchingservice.entities.OutboxEvent;
import com.example.matchingservice.repository.DeliveryOfferRepository;
import com.example.matchingservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.event.AddressEventDto;
import org.example.event.ShipmentCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingEngineServiceTest {

    @Mock
    private TrackingClientService trackingClientService;

    @Mock
    private DeliveryOfferRepository offerRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MatchingEngineService matchingEngineService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(matchingEngineService, "defaultRadiusKm", 10.0);
        ReflectionTestUtils.setField(matchingEngineService, "offerExpiryMinutes", 5);
    }

    @Test
    void testProcessShipmentCreated_CandidatesFound_CreatesOffersAndOutbox() throws Exception {
        ShipmentCreatedEvent event = ShipmentCreatedEvent.builder()
                .eventId("EVT-1")
                .shipmentId("SHP-001")
                .pickupAddress(AddressEventDto.builder().geoLocation("18.5204,73.8567").build())
                .build();

        NearbyDriverDto candidate = NearbyDriverDto.builder()
                .driverId("DRV-101")
                .vehicleId("VEH-101")
                .latitude(18.5205)
                .longitude(73.8568)
                .distanceKm(2.5)
                .build();

        when(trackingClientService.getNearbyDrivers(18.5204, 73.8567, 10.0))
                .thenReturn(List.of(candidate));
        when(offerRepository.save(any(DeliveryOffer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"eventType\":\"DELIVERY_OFFER_CREATED\"}");

        matchingEngineService.processShipmentCreated(event);

        verify(offerRepository, times(1)).save(any(DeliveryOffer.class));
        verify(outboxEventRepository, times(1)).save(any(OutboxEvent.class));
    }

    @Test
    void testProcessShipmentCreated_NoCandidates_DoesNotCreateOffers() {
        ShipmentCreatedEvent event = ShipmentCreatedEvent.builder()
                .eventId("EVT-1")
                .shipmentId("SHP-002")
                .pickupAddress(AddressEventDto.builder().geoLocation("18.5204,73.8567").build())
                .build();

        when(trackingClientService.getNearbyDrivers(18.5204, 73.8567, 10.0))
                .thenReturn(Collections.emptyList());

        matchingEngineService.processShipmentCreated(event);

        verify(offerRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void testProcessShipmentCreated_MissingGeoLocation_GracefullySkips() {
        ShipmentCreatedEvent event = ShipmentCreatedEvent.builder()
                .eventId("EVT-1")
                .shipmentId("SHP-003")
                .pickupAddress(AddressEventDto.builder().geoLocation(null).build())
                .build();

        matchingEngineService.processShipmentCreated(event);

        verify(trackingClientService, never()).getNearbyDrivers(anyDouble(), anyDouble(), anyDouble());
        verify(offerRepository, never()).save(any());
    }
}
