package com.example.shipmentservice.dto;

import com.example.shipmentservice.entities.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentStatusUpdateDto {

    private ShipmentStatus status;
    private String driverId;
    private String vehicleId;
    private String cancellationReason;
}
