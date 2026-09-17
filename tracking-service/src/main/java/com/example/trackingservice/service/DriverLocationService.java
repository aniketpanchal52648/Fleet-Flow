package com.example.trackingservice.service;

import com.example.trackingservice.documents.DriverLocationDocument;
import com.example.trackingservice.dto.DriverLocationEventDto;
import com.example.trackingservice.dto.DriverPresenceDto;
import com.example.trackingservice.entities.DriverTrackingSession;
import com.example.trackingservice.entities.SessionStatus;
import com.example.trackingservice.repository.DriverLocationMongoRepository;
import com.example.trackingservice.repository.DriverTrackingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorDetail;
import org.example.error.ErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverLocationService {

    private final DriverLocationMongoRepository mongoRepository;
    private final DriverTrackingSessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.tracking.redis.geo-key:geo:available-drivers}")
    private String geoKey;

    @Value("${app.tracking.redis.presence-prefix:driver:presence:}")
    private String presencePrefix;

    @Transactional
    public void recordLocation(DriverLocationEventDto event) {
        // 1. Deduplication check
        if (mongoRepository.existsByEventId(event.getEventId())) {
            log.warn("Duplicate location event [{}] ignored", event.getEventId());
            return;
        }

        // 2. Verify Driver has an ACTIVE or STALE session (or auto-heal fresh session)
        DriverTrackingSession session = sessionRepository.findByDriverIdAndStatus(event.getDriverId(), SessionStatus.ACTIVE)
                .or(() -> sessionRepository.findByDriverIdAndStatus(event.getDriverId(), SessionStatus.STALE))
                .orElseGet(() -> {
                    log.info("Auto-healing: Driver [{}] has no active session. Auto-starting new active session.", event.getDriverId());
                    DriverTrackingSession newSession = DriverTrackingSession.builder()
                            .driverId(event.getDriverId())
                            .vehicleId(event.getVehicleId())
                            .status(SessionStatus.ACTIVE)
                            .lastSeenAt(LocalDateTime.now())
                            .build();
                    return sessionRepository.save(newSession);
                });

        // 3. Persist Raw Telemetry in MongoDB
        DriverLocationDocument doc = DriverLocationDocument.builder()
                .eventId(event.getEventId())
                .driverId(event.getDriverId())
                .vehicleId(event.getVehicleId())
                .sessionId(session.getSessionId())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .accuracyMeters(event.getAccuracyMeters())
                .speedKph(event.getSpeedKph())
                .bearing(event.getBearing())
                .recordedAt(event.getRecordedAt())
                .receivedAt(Instant.now())
                .activeDeliveryJobId(event.getActiveDeliveryJobId())
                .source("MOBILE_APP")
                .build();
        mongoRepository.save(doc);

        // 4. Update PostgreSQL Session 'last_seen_at' and restore to ACTIVE if it was STALE
        session.setLastSeenAt(event.getRecordedAt());
        session.setStatus(SessionStatus.ACTIVE);
        sessionRepository.save(session);

        // 5. Update Redis GEO Index (Point: longitude, latitude)
        Point point = new Point(event.getLongitude(), event.getLatitude());
        redisTemplate.opsForGeo().add(geoKey, point, event.getDriverId());

        // 6. Update Redis Presence Cache
        DriverPresenceDto presence = DriverPresenceDto.builder()
                .driverId(event.getDriverId())
                .vehicleId(event.getVehicleId())
                .sessionId(session.getSessionId())
                .presenceStatus("ONLINE")
                .lastSeenAt(event.getRecordedAt())
                .lastLatitude(event.getLatitude())
                .lastLongitude(event.getLongitude())
                .build();
        redisTemplate.opsForValue().set(presencePrefix + event.getDriverId(), presence);

        log.debug("Recorded GPS for driver [{}] at lat:{}, lon:{}", event.getDriverId(), event.getLatitude(), event.getLongitude());
    }
}
