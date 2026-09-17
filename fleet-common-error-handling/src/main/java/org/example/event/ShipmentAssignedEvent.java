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
public class ShipmentAssignedEvent {
    private String eventId;          // UUID for consumer deduplication
    private String eventType;        // "SHIPMENT_ASSIGNED"
    private String shipmentId;       // Partition key (e.g. SHP-A1B2C3D4)
    private String userId;           // Customer userId
    private String driverId;         // Assigned driverId
    private String vehicleId;        // Assigned vehicleId
    private LocalDateTime assignedAt;
}
