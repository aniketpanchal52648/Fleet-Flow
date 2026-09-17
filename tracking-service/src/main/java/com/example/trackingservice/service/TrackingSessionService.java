package com.example.trackingservice.service;

import com.example.trackingservice.dto.DriverPresenceDto;
import com.example.trackingservice.dto.StartSessionRequestDto;
import com.example.trackingservice.dto.StopSessionRequestDto;
import com.example.trackingservice.entities.DriverTrackingSession;
import com.example.trackingservice.entities.SessionStatus;
import com.example.trackingservice.repository.DriverTrackingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorDetail;
import org.example.error.ErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingSessionService {

    private final DriverTrackingSessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${app.tracking.redis.geo-key:geo:available-drivers}")
    private String geoKey;

    @Value("${app.tracking.redis.presence-prefix:driver:presence:}")
    private String presencePrefix;

    @Transactional
    public DriverPresenceDto startSession(StartSessionRequestDto request) {
        String driverId = request.getDriverId();
        String vehicleId = request.getVehicleId();

        // 1. Resilient Auto-Close of any previous ACTIVE or STALE session
        List<DriverTrackingSession> existingSessions = sessionRepository.findAllByDriverIdAndStatus(driverId, SessionStatus.ACTIVE);
        existingSessions.addAll(sessionRepository.findAllByDriverIdAndStatus(driverId, SessionStatus.STALE));

        for (DriverTrackingSession s : existingSessions) {
            s.setStatus(SessionStatus.STOPPED);
            s.setStoppedAt(LocalDateTime.now());
            s.setStopReason("REPLACED_BY_NEW_SESSION");
            sessionRepository.save(s);
            log.info("Closed previous session [{}] for driver [{}]", s.getSessionId(), driverId);
        }

        // 2. Create and persist new ACTIVE session in PostgreSQL
        DriverTrackingSession session = DriverTrackingSession.builder()
                .driverId(driverId)
                .vehicleId(vehicleId)
                .status(SessionStatus.ACTIVE)
                .lastSeenAt(LocalDateTime.now())
                .build();
        DriverTrackingSession saved = sessionRepository.save(session);

        // 3. Update Redis Presence Cache
        DriverPresenceDto presence = DriverPresenceDto.builder()
                .driverId(driverId)
                .vehicleId(vehicleId)
                .sessionId(saved.getSessionId())
                .presenceStatus("ONLINE")
                .lastSeenAt(LocalDateTime.now())
                .build();
        redisTemplate.opsForValue().set(presencePrefix + driverId, presence);

        log.info("Started new tracking session [{}] for driver [{}]", saved.getSessionId(), driverId);
        return presence;
    }

    @Transactional
    public DriverPresenceDto stopSession(StopSessionRequestDto request) {
        String driverId = request.getDriverId();

        // 1. Find active or stale sessions and mark STOPPED
        List<DriverTrackingSession> activeSessions = sessionRepository.findAllByDriverIdAndStatus(driverId, SessionStatus.ACTIVE);
        activeSessions.addAll(sessionRepository.findAllByDriverIdAndStatus(driverId, SessionStatus.STALE));

        if (activeSessions.isEmpty()) {
            throw new ErrorException(new ErrorDetail("NO_ACTIVE_SESSION", "driverId", "No active session found for driver: " + driverId));
        }

        for (DriverTrackingSession s : activeSessions) {
            s.setStatus(SessionStatus.STOPPED);
            s.setStoppedAt(LocalDateTime.now());
            s.setStopReason(request.getReason() != null ? request.getReason() : "DRIVER_OFFLINE");
            sessionRepository.save(s);
        }

        // 2. Remove driver from Redis GEO index
        redisTemplate.opsForZSet().remove(geoKey, driverId);

        // 3. Update presence status in Redis to OFFLINE
        DriverPresenceDto presence = DriverPresenceDto.builder()
                .driverId(driverId)
                .presenceStatus("OFFLINE")
                .lastSeenAt(LocalDateTime.now())
                .build();
        redisTemplate.opsForValue().set(presencePrefix + driverId, presence);

        log.info("Driver [{}] went OFFLINE and was removed from Redis GEO", driverId);
        return presence;
    }

    public DriverPresenceDto getPresence(String driverId) {
        Object cached = redisTemplate.opsForValue().get(presencePrefix + driverId);
        if (cached == null) {
            return DriverPresenceDto.builder()
                    .driverId(driverId)
                    .presenceStatus("OFFLINE")
                    .build();
        }
        if (cached instanceof DriverPresenceDto) {
            return (DriverPresenceDto) cached;
        }
        try {
            return objectMapper.convertValue(cached, DriverPresenceDto.class);
        } catch (Exception e) {
            log.error("Error converting cached presence for driver {}: {}", driverId, e.getMessage());
            return DriverPresenceDto.builder()
                    .driverId(driverId)
                    .presenceStatus("OFFLINE")
                    .build();
        }
    }
}
