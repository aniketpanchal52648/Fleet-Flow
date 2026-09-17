package com.example.trackingservice.service;

import com.example.trackingservice.dto.DriverPresenceDto;
import com.example.trackingservice.entities.DriverTrackingSession;
import com.example.trackingservice.entities.SessionStatus;
import com.example.trackingservice.repository.DriverTrackingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaleDriverDetectionScheduler {

    private final DriverTrackingSessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${app.tracking.redis.geo-key:geo:available-drivers}")
    private String geoKey;

    @Value("${app.tracking.redis.presence-prefix:driver:presence:}")
    private String presencePrefix;

    @Value("${app.tracking.stale-threshold-minutes:2}")
    private int staleThresholdMinutes;

    @Value("${app.tracking.offline-threshold-minutes:5}")
    private int offlineThresholdMinutes;

    @Scheduled(fixedDelay = 60000) // Runs every 60 seconds
    @Transactional
    public void cleanupStaleAndOfflineDrivers() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Find sessions with no update for > 2 minutes -> Mark STALE
        LocalDateTime staleThreshold = now.minusMinutes(staleThresholdMinutes);
        List<DriverTrackingSession> activeSessions = sessionRepository.findAllByStatusAndLastSeenAtBefore(SessionStatus.ACTIVE, staleThreshold);

        for (DriverTrackingSession s : activeSessions) {
            s.setStatus(SessionStatus.STALE);
            sessionRepository.save(s);

            Object presenceObj = redisTemplate.opsForValue().get(presencePrefix + s.getDriverId());
            DriverPresenceDto presence = null;
            if (presenceObj instanceof DriverPresenceDto) {
                presence = (DriverPresenceDto) presenceObj;
            } else if (presenceObj != null) {
                try {
                    presence = objectMapper.convertValue(presenceObj, DriverPresenceDto.class);
                } catch (Exception ignored) {}
            }

            if (presence != null) {
                presence.setPresenceStatus("STALE");
                redisTemplate.opsForValue().set(presencePrefix + s.getDriverId(), presence);
            }
            log.warn("Driver [{}] marked STALE (no GPS update for > {} min)", s.getDriverId(), staleThresholdMinutes);
        }

        // 2. Find sessions with no update for > 5 minutes -> Mark STOPPED & remove from Redis GEO
        LocalDateTime offlineThreshold = now.minusMinutes(offlineThresholdMinutes);
        List<DriverTrackingSession> staleSessions = sessionRepository.findAllByStatusAndLastSeenAtBefore(SessionStatus.STALE, offlineThreshold);

        for (DriverTrackingSession s : staleSessions) {
            s.setStatus(SessionStatus.STOPPED);
            s.setStoppedAt(now);
            s.setStopReason("INACTIVITY_TIMEOUT");
            sessionRepository.save(s);

            // Remove from Redis GEO index & mark presence OFFLINE
            redisTemplate.opsForZSet().remove(geoKey, s.getDriverId());

            DriverPresenceDto presence = DriverPresenceDto.builder()
                    .driverId(s.getDriverId())
                    .vehicleId(s.getVehicleId())
                    .sessionId(s.getSessionId())
                    .presenceStatus("OFFLINE")
                    .lastSeenAt(s.getLastSeenAt())
                    .build();
            redisTemplate.opsForValue().set(presencePrefix + s.getDriverId(), presence);

            log.warn("Driver [{}] session stopped due to timeout. Removed from Redis GEO.", s.getDriverId());
        }
    }
}
