package com.example.trackingservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DriverLocationEventDto {

    @NotBlank(message = "eventId is required")
    private String eventId;

    @NotBlank(message = "driverId is required")
    private String driverId;

    @NotBlank(message = "vehicleId is required")
    private String vehicleId;

    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0", message = "latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "latitude must be <= 90.0")
    private Double latitude;

    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "longitude must be <= 180.0")
    private Double longitude;

    private Double accuracyMeters;
    private Double speedKph;
    private Double bearing;

    @NotNull(message = "recordedAt is required")
    private LocalDateTime recordedAt;

    private String activeDeliveryJobId;
}
