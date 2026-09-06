package com.example.fleetpartner.util;

import com.example.fleetpartner.dto.DriverVehicleAssignmentDto;
import com.example.fleetpartner.entites.DriverVehicleAssignment;

public class DriverVehicleAssignmentUtil {

    public static DriverVehicleAssignmentDto mapToDto(DriverVehicleAssignment assignment) {
        if (assignment == null) {
            return null;
        }
        return DriverVehicleAssignmentDto.builder()
                .id(assignment.getId())
                .assignmentId(assignment.getAssignmentId())
                .vehicleId(assignment.getVehicleId())
                .driverId(assignment.getDriverId())
                .assignedFrom(assignment.getAssignedFrom())
                .assignedTo(assignment.getAssignedTo())
                .status(assignment.getStatus())
                .endReason(assignment.getEndReason())
                .build();
    }
}
