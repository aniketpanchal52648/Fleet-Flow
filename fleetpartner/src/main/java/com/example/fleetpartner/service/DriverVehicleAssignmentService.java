package com.example.fleetpartner.service;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.AssignmentRequestDto;
import com.example.fleetpartner.dto.DriverVehicleAssignmentDto;
import com.example.fleetpartner.dto.UnassignRequestDto;
import com.example.fleetpartner.entites.Status;

import java.util.List;

public interface DriverVehicleAssignmentService {

    DriverVehicleAssignmentDto assignDriver(AssignmentRequestDto request);

    ApiResponseDto unassignDriver(String assignmentId, UnassignRequestDto request);

    DriverVehicleAssignmentDto getActiveAssignmentForVehicle(String vehicleId);

    DriverVehicleAssignmentDto getActiveAssignmentForDriver(String driverId);

    List<DriverVehicleAssignmentDto> getAssignmentHistory(String vehicleId, String driverId, Status status);
}
