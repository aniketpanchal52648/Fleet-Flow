package com.example.trackingservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StopSessionRequestDto {
    @NotBlank(message = "driverId is required")
    private String driverId;
    private String reason;
}
