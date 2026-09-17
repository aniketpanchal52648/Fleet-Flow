package com.example.trackingservice.service;

import com.example.trackingservice.dto.DriverPresenceDto;
import com.example.trackingservice.dto.ShipmentLiveTrackingResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorDetail;
import org.example.error.ErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentTrackingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.tracking.redis.geo-key:geo:available-drivers}")
    private String geoKey;

    @Value("${app.tracking.redis.presence-prefix:driver:presence:}")
    private String presencePrefix;

    @Value("${app.tracking.redis.shipment-driver-prefix:shipment:driver:}")
    private String shipmentDriverPrefix;

    @Value("${app.tracking.redis.driver-shipment-prefix:driver:shipment:}")
    private String driverShipmentPrefix;

    public void assignDriverToShipment(String shipmentId, String driverId, String vehicleId) {
        log.info("Assigning driver [{}] (vehicle: {}) to shipment [{}]", driverId, vehicleId, shipmentId);

        // 1. Store bi-directional association in Redis
        redisTemplate.opsForValue().set(shipmentDriverPrefix + shipmentId, driverId);
        redisTemplate.opsForValue().set(driverShipmentPrefix + driverId, shipmentId);

        // 2. Remove driver from available GEO pool
        Long removedCount = redisTemplate.opsForGeo().remove(geoKey, driverId);
        log.info("Removed driver [{}] from GEO key [{}] (count: {})", driverId, geoKey, removedCount);

        // 3. Update driver presence cache to 'ON_TRIP'
        String presenceKey = presencePrefix + driverId;
        Object cachedPresence = redisTemplate.opsForValue().get(presenceKey);
        DriverPresenceDto presence = null;

        if (cachedPresence instanceof DriverPresenceDto) {
            presence = (DriverPresenceDto) cachedPresence;
        } else if (cachedPresence != null) {
            try {
                presence = objectMapper.convertValue(cachedPresence, DriverPresenceDto.class);
            } catch (Exception e) {
                log.warn("Failed to parse cached presence for driver [{}]: {}", driverId, e.getMessage());
            }
        }

        if (presence == null) {
            presence = DriverPresenceDto.builder()
                    .driverId(driverId)
                    .vehicleId(vehicleId)
                    .presenceStatus("ON_TRIP")
                    .lastSeenAt(LocalDateTime.now())
                    .build();
        } else {
            presence.setPresenceStatus("ON_TRIP");
            if (vehicleId != null) {
                presence.setVehicleId(vehicleId);
            }
        }

        redisTemplate.opsForValue().set(presenceKey, presence);
        log.info("Updated driver [{}] presence status to ON_TRIP in Redis", driverId);
    }

    public ShipmentLiveTrackingResponseDto getShipmentLiveLocation(String shipmentId) {
        log.info("Fetching live tracking for shipment [{}]", shipmentId);

        // 1. Look up assigned driverId from Redis
        Object assignedDriverObj = redisTemplate.opsForValue().get(shipmentDriverPrefix + shipmentId);
        if (assignedDriverObj == null) {
            throw new ErrorException(ErrorDetail.builder()
                    .code("SHIPMENT_NOT_ASSIGNED")
                    .field("shipmentId")
                    .message("No active driver assigned to shipment [" + shipmentId + "]")
                    .build());
        }

        String driverId = String.valueOf(assignedDriverObj);

        // 2. Look up driver presence
        Object cachedPresence = redisTemplate.opsForValue().get(presencePrefix + driverId);
        DriverPresenceDto presence = null;

        if (cachedPresence instanceof DriverPresenceDto) {
            presence = (DriverPresenceDto) cachedPresence;
        } else if (cachedPresence != null) {
            try {
                presence = objectMapper.convertValue(cachedPresence, DriverPresenceDto.class);
            } catch (Exception ignored) {}
        }

        if (presence == null) {
            return ShipmentLiveTrackingResponseDto.builder()
                    .shipmentId(shipmentId)
                    .driverId(driverId)
                    .presenceStatus("UNKNOWN")
                    .build();
        }

        return ShipmentLiveTrackingResponseDto.builder()
                .shipmentId(shipmentId)
                .driverId(driverId)
                .vehicleId(presence.getVehicleId())
                .latitude(presence.getLastLatitude())
                .longitude(presence.getLastLongitude())
                .presenceStatus(presence.getPresenceStatus())
                .lastSeenAt(presence.getLastSeenAt())
                .build();
    }
}
