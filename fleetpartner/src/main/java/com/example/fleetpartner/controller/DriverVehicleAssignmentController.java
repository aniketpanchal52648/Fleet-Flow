package com.example.fleetpartner.controller;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.AssignmentRequestDto;
import com.example.fleetpartner.dto.DriverVehicleAssignmentDto;
import com.example.fleetpartner.dto.UnassignRequestDto;
import com.example.fleetpartner.entites.Status;
import com.example.fleetpartner.service.DriverVehicleAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("web/v1/fleet-partner-service/assignment")
@RequiredArgsConstructor
public class DriverVehicleAssignmentController {

    private final DriverVehicleAssignmentService assignmentService;

    @PostMapping("/assign")
    public ResponseEntity<DriverVehicleAssignmentDto> assignDriver(@RequestBody AssignmentRequestDto request) {
        DriverVehicleAssignmentDto assignment = assignmentService.assignDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @PatchMapping("/{assignmentId}/unassign")
    public ResponseEntity<ApiResponseDto> unassignDriver(
            @PathVariable String assignmentId,
            @RequestBody(required = false) UnassignRequestDto request) {
        return ResponseEntity.ok(assignmentService.unassignDriver(assignmentId, request));
    }

    @GetMapping("/active/vehicle/{vehicleId}")
    public ResponseEntity<DriverVehicleAssignmentDto> getActiveAssignmentForVehicle(@PathVariable String vehicleId) {
        return ResponseEntity.ok(assignmentService.getActiveAssignmentForVehicle(vehicleId));
    }

    @GetMapping("/active/driver/{driverId}")
    public ResponseEntity<DriverVehicleAssignmentDto> getActiveAssignmentForDriver(@PathVariable String driverId) {
        return ResponseEntity.ok(assignmentService.getActiveAssignmentForDriver(driverId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<DriverVehicleAssignmentDto>> getAssignmentHistory(
            @RequestParam(required = false) String vehicleId,
            @RequestParam(required = false) String driverId,
            @RequestParam(required = false) Status status) {
        return ResponseEntity.ok(assignmentService.getAssignmentHistory(vehicleId, driverId, status));
    }
}
