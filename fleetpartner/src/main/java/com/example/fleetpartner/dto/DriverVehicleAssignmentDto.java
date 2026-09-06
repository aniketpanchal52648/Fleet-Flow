package com.example.fleetpartner.dto;

import com.example.fleetpartner.entites.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverVehicleAssignmentDto {

    private String id;
    private String assignmentId;
    private String vehicleId;
    private String driverId;
    private LocalDateTime assignedFrom;
    private LocalDateTime assignedTo;
    private Status status;
    private String endReason;
}
