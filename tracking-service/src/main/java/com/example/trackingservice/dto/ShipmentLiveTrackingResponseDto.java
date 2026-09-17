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
public class ShipmentLiveTrackingResponseDto {
    private String shipmentId;
    private String driverId;
    private String vehicleId;
    private Double latitude;
    private Double longitude;
    private Double speedKph;
    private Double bearing;
    private String presenceStatus; // "ON_TRIP", "ONLINE", "OFFLINE"
    private LocalDateTime lastSeenAt;
}
