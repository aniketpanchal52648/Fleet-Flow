package com.example.trackingservice.service;

import com.example.trackingservice.dto.DriverPresenceDto;
import com.example.trackingservice.dto.NearbyDriverResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NearbyDriverService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${app.tracking.redis.geo-key:geo:available-drivers}")
    private String geoKey;

    @Value("${app.tracking.redis.presence-prefix:driver:presence:}")
    private String presencePrefix;

    public List<NearbyDriverResponseDto> findNearbyDrivers(Double latitude, Double longitude, Double radiusKm) {
        List<NearbyDriverResponseDto> nearbyList = new ArrayList<>();

        Point center = new Point(longitude, latitude);
        Distance distance = new Distance(radiusKm, Metrics.KILOMETERS);
        Circle circle = new Circle(center, distance);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending();

        log.info("Redis GEO query on key [{}] center [lon:{}, lat:{}] radius {} km", geoKey, longitude, latitude, radiusKm);
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisTemplate.opsForGeo().radius(geoKey, circle, args);

        if (results == null || results.getContent().isEmpty()) {
            log.warn("Redis GEO returned 0 results for key [{}]", geoKey);
            return nearbyList;
        }

        log.info("Redis GEO returned {} raw candidate(s)", results.getContent().size());

        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> res : results.getContent()) {
            String driverId = String.valueOf(res.getContent().getName());
            Double dist = res.getDistance().getValue();
            Point coord = res.getContent().getPoint();

            Object cachedPresence = redisTemplate.opsForValue().get(presencePrefix + driverId);
            DriverPresenceDto presence = null;
            if (cachedPresence instanceof DriverPresenceDto) {
                presence = (DriverPresenceDto) cachedPresence;
            } else if (cachedPresence != null) {
                try {
                    presence = objectMapper.convertValue(cachedPresence, DriverPresenceDto.class);
                } catch (Exception ignored) {}
            }

            log.info("Candidate driver [{}]: dist={} km, presenceStatus={}", driverId, dist, presence != null ? presence.getPresenceStatus() : "null");

            if (presence != null && ("ONLINE".equalsIgnoreCase(presence.getPresenceStatus()) || "STALE".equalsIgnoreCase(presence.getPresenceStatus()))) {
                nearbyList.add(NearbyDriverResponseDto.builder()
                        .driverId(driverId)
                        .vehicleId(presence.getVehicleId())
                        .distanceKm(Math.round(dist * 100.0) / 100.0)
                        .latitude(coord != null ? coord.getY() : null)
                        .longitude(coord != null ? coord.getX() : null)
                        .presenceStatus(presence.getPresenceStatus())
                        .lastSeenAt(presence.getLastSeenAt())
                        .build());
            }
        }

        log.info("Returning [{}] nearby drivers", nearbyList.size());
        return nearbyList;
    }
}
