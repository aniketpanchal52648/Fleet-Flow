package com.example.shipmentservice.dto;

import com.example.shipmentservice.entities.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserShipmentDto {

    private String id;
    private String shipmentId;
    private String userId;
    private UserAddressDto pickupLocation;
    private UserAddressDto dropLocation;
    private ShipmentStatus shipmentStatus;
    private String driverId;
    private String vehicleId;
    private String vehicleType;
    private Double weightKg;
    private String cancellationReason;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
