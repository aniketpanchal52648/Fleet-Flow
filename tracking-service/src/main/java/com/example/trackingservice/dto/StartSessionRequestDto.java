package com.example.trackingservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartSessionRequestDto {
    @NotBlank(message = "driverId is required")
    private String driverId;

    @NotBlank(message = "vehicleId is required")
    private String vehicleId;
}
