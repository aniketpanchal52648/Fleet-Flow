package com.example.trackingservice.service;

import com.example.trackingservice.dto.DriverPresenceDto;
import com.example.trackingservice.dto.ShipmentLiveTrackingResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.error.ErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentTrackingServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private GeoOperations<String, Object> geoOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ShipmentTrackingService trackingService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForGeo()).thenReturn(geoOperations);

        trackingService = new ShipmentTrackingService(redisTemplate, objectMapper);
        ReflectionTestUtils.setField(trackingService, "geoKey", "geo:available-drivers");
        ReflectionTestUtils.setField(trackingService, "presencePrefix", "driver:presence:");
        ReflectionTestUtils.setField(trackingService, "shipmentDriverPrefix", "shipment:driver:");
        ReflectionTestUtils.setField(trackingService, "driverShipmentPrefix", "driver:shipment:");
    }

    @Test
    @DisplayName("Should assign driver to shipment, update Redis mappings, remove from GEO pool, and set status to ON_TRIP")
    void testAssignDriverToShipment() {
        DriverPresenceDto existingPresence = DriverPresenceDto.builder()
                .driverId("DRV-101")
                .vehicleId("VEH-101")
                .presenceStatus("ONLINE")
                .lastLatitude(19.0760)
                .lastLongitude(72.8777)
                .build();

        when(valueOperations.get("driver:presence:DRV-101")).thenReturn(existingPresence);
        when(geoOperations.remove("geo:available-drivers", "DRV-101")).thenReturn(1L);

        trackingService.assignDriverToShipment("SHP-999", "DRV-101", "VEH-101");

        verify(valueOperations).set("shipment:driver:SHP-999", "DRV-101");
        verify(valueOperations).set("driver:shipment:DRV-101", "SHP-999");
        verify(geoOperations).remove("geo:available-drivers", "DRV-101");
        verify(valueOperations).set(eq("driver:presence:DRV-101"), any(DriverPresenceDto.class));
    }

    @Test
    @DisplayName("Should return live tracking response for assigned shipment")
    void testGetShipmentLiveLocationSuccess() {
        DriverPresenceDto presence = DriverPresenceDto.builder()
                .driverId("DRV-101")
                .vehicleId("VEH-101")
                .presenceStatus("ON_TRIP")
                .lastLatitude(19.0760)
                .lastLongitude(72.8777)
                .lastSeenAt(LocalDateTime.now())
                .build();

        when(valueOperations.get("shipment:driver:SHP-999")).thenReturn("DRV-101");
        when(valueOperations.get("driver:presence:DRV-101")).thenReturn(presence);

        ShipmentLiveTrackingResponseDto response = trackingService.getShipmentLiveLocation("SHP-999");

        assertNotNull(response);
        assertEquals("SHP-999", response.getShipmentId());
        assertEquals("DRV-101", response.getDriverId());
        assertEquals("VEH-101", response.getVehicleId());
        assertEquals(19.0760, response.getLatitude());
        assertEquals(72.8777, response.getLongitude());
        assertEquals("ON_TRIP", response.getPresenceStatus());
    }

    @Test
    @DisplayName("Should throw ErrorException when shipment is not assigned to any driver")
    void testGetShipmentLiveLocationNotAssigned() {
        when(valueOperations.get("shipment:driver:SHP-UNASSIGNED")).thenReturn(null);

        assertThrows(ErrorException.class, () -> trackingService.getShipmentLiveLocation("SHP-UNASSIGNED"));
    }
}
