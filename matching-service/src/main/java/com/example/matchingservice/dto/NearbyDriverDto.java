package com.example.matchingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyDriverDto {
    private String driverId;
    private String vehicleId;
    private Double distanceKm;
    private Double latitude;
    private Double longitude;
    private String presenceStatus;
    private LocalDateTime lastSeenAt;
}
