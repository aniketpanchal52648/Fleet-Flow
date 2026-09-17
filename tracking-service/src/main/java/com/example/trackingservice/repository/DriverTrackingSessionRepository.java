package com.example.trackingservice.repository;

import com.example.trackingservice.entities.DriverTrackingSession;
import com.example.trackingservice.entities.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverTrackingSessionRepository extends JpaRepository<DriverTrackingSession, String> {

    Optional<DriverTrackingSession> findByDriverIdAndStatus(String driverId, SessionStatus status);

    List<DriverTrackingSession> findAllByDriverIdAndStatus(String driverId, SessionStatus status);

    List<DriverTrackingSession> findAllByStatusAndLastSeenAtBefore(SessionStatus status, LocalDateTime threshold);
}
