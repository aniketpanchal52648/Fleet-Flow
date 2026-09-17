package com.example.trackingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverPresenceDto {
    private String driverId;
    private String vehicleId;
    private String sessionId;
    private String presenceStatus; // ONLINE, STALE, OFFLINE
    private LocalDateTime lastSeenAt;
    private Double lastLatitude;
    private Double lastLongitude;
}
