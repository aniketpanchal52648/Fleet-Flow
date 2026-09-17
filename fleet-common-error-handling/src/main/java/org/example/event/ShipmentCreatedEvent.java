package org.example.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentCreatedEvent {
    private String eventId;          // UUID for consumer deduplication
    private String eventType;        // "SHIPMENT_CREATED"
    private String shipmentId;       // Partition key (e.g. SHP-A1B2C3D4)
    private String userId;
    private String vehicleType;
    private Double weightKg;
    private AddressEventDto pickupAddress;
    private AddressEventDto dropAddress;
    private String status;           // "CREATED"
    private LocalDateTime createdAt;
}