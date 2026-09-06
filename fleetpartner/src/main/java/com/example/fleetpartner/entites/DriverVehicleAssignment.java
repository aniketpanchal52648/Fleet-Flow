package com.example.fleetpartner.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "DRIVER_VEHICLE_ASSIGNMENT")
@Data
public class DriverVehicleAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String assignmentId;
    private String vehicleId;
    private String driverId;
    private LocalDateTime assignedFrom;
    private LocalDateTime assignedTo;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String endReason;
}
