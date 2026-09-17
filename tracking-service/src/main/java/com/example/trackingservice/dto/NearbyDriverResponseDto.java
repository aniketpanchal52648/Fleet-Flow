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
public class NearbyDriverResponseDto {
    private String driverId;
    private String vehicleId;
    private Double distanceKm;
    private Double latitude;
    private Double longitude;
    private String presenceStatus;
    private LocalDateTime lastSeenAt;
}
