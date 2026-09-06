package com.example.fleetpartner.service.impl;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.AssignmentRequestDto;
import com.example.fleetpartner.dto.DriverVehicleAssignmentDto;
import com.example.fleetpartner.dto.UnassignRequestDto;
import com.example.fleetpartner.entites.Driver;
import com.example.fleetpartner.entites.DriverVehicleAssignment;
import com.example.fleetpartner.entites.Status;
import com.example.fleetpartner.entites.Vehicle;
import com.example.fleetpartner.repository.DriverRepository;
import com.example.fleetpartner.repository.DriverVehicleAssignmentRepository;
import com.example.fleetpartner.repository.VehicleRepository;
import com.example.fleetpartner.service.DriverVehicleAssignmentService;
import com.example.fleetpartner.util.DriverVehicleAssignmentUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorDetail;
import org.example.error.ErrorException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DriverVehicleAssignmentServiceImpl implements DriverVehicleAssignmentService {

    private final DriverVehicleAssignmentRepository assignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Override
    public DriverVehicleAssignmentDto assignDriver(AssignmentRequestDto request) {
        if (request.getVehicleId() == null || request.getVehicleId().trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("FP_VEHICLE_ID_REQUIRED", "vehicleId", "Vehicle ID is required"));
        }
        if (request.getDriverId() == null || request.getDriverId().trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("FP_DRIVER_ID_REQUIRED", "driverId", "Driver ID is required"));
        }

        String vehicleId = request.getVehicleId().trim();
        String driverId = request.getDriverId().trim();

        // 1. Validate Vehicle
        Vehicle vehicle = vehicleRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("FP_VEHICLE_NOT_FOUND", "vehicleId",
                        "Vehicle not found with vehicleId: " + vehicleId)));

        if (vehicle.getStatus() != Status.ACTIVE) {
            throw new ErrorException(new ErrorDetail("FP_VEHICLE_NOT_ACTIVE", "vehicleId",
                    "Vehicle is not in ACTIVE status. Current status: " + vehicle.getStatus()));
        }

        if (assignmentRepository.existsByVehicleIdAndStatus(vehicleId, Status.ACTIVE)) {
            throw new ErrorException(new ErrorDetail("FP_VEHICLE_ALREADY_ASSIGNED", "vehicleId",
                    "Vehicle already has an active driver assigned"));
        }

        // 2. Validate Driver
        Driver driver = driverRepository.findByDriverId(driverId)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("FP_DRIVER_NOT_FOUND", "driverId",
                        "Driver not found with driverId: " + driverId)));

        if (driver.getStatus() != Status.ACTIVE) {
            throw new ErrorException(new ErrorDetail("FP_DRIVER_NOT_ACTIVE", "driverId",
                    "Driver is not in ACTIVE status. Current status: " + driver.getStatus()));
        }

        if (driver.getLicencesExpiry() != null && driver.getLicencesExpiry().before(new Date())) {
            throw new ErrorException(new ErrorDetail("FP_DRIVER_LICENCE_EXPIRED", "driverId",
                    "Driver driving licence is expired"));
        }

        if (assignmentRepository.existsByDriverIdAndStatus(driverId, Status.ACTIVE)) {
            throw new ErrorException(new ErrorDetail("FP_DRIVER_ALREADY_ASSIGNED", "driverId",
                    "Driver already has an active vehicle assigned"));
        }

        // 3. Validate Fleet Partner matching rule
        String vehiclePartnerId = vehicle.getFleetPartner() != null ? vehicle.getFleetPartner().getPartnerId() : null;
        String driverPartnerId = driver.getPartnerId();

        if (vehiclePartnerId != null && driverPartnerId != null && !vehiclePartnerId.equalsIgnoreCase(driverPartnerId)) {
            throw new ErrorException(new ErrorDetail("FP_PARTNER_MISMATCH", "partnerId",
                    "Vehicle and Driver belong to different fleet partners (" + vehiclePartnerId + " vs " + driverPartnerId + ")"));
        }

        // 4. Create and persist assignment
        DriverVehicleAssignment assignment = new DriverVehicleAssignment();
        assignment.setAssignmentId("ASG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        assignment.setVehicleId(vehicleId);
        assignment.setDriverId(driverId);
        assignment.setAssignedFrom(LocalDateTime.now());
        assignment.setStatus(Status.ACTIVE);

        DriverVehicleAssignment saved = assignmentRepository.save(assignment);
        log.info("Assigned driver {} to vehicle {} with assignmentId: {}", driverId, vehicleId, saved.getAssignmentId());

        return DriverVehicleAssignmentUtil.mapToDto(saved);
    }

    @Override
    public ApiResponseDto unassignDriver(String assignmentId, UnassignRequestDto request) {
        DriverVehicleAssignment assignment = assignmentRepository.findByAssignmentId(assignmentId)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("FP_ASSIGNMENT_NOT_FOUND", "assignmentId",
                        "Assignment not found with assignmentId: " + assignmentId)));

        if (assignment.getStatus() != Status.ACTIVE) {
            throw new ErrorException(new ErrorDetail("FP_ASSIGNMENT_NOT_ACTIVE", "assignmentId",
                    "Assignment is not currently active. Current status: " + assignment.getStatus()));
        }

        assignment.setAssignedTo(LocalDateTime.now());
        assignment.setStatus(Status.INACTIVE);
        assignment.setEndReason(request != null && request.getEndReason() != null && !request.getEndReason().trim().isEmpty()
                ? request.getEndReason().trim()
                : "UNASSIGNED");

        assignmentRepository.save(assignment);
        log.info("Unassigned assignmentId: {}", assignmentId);

        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Driver successfully unassigned from vehicle")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DriverVehicleAssignmentDto getActiveAssignmentForVehicle(String vehicleId) {
        DriverVehicleAssignment assignment = assignmentRepository.findByVehicleIdAndStatus(vehicleId, Status.ACTIVE)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("FP_ASSIGNMENT_NOT_FOUND", "vehicleId",
                        "No active driver assignment found for vehicle: " + vehicleId)));
        return DriverVehicleAssignmentUtil.mapToDto(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverVehicleAssignmentDto getActiveAssignmentForDriver(String driverId) {
        DriverVehicleAssignment assignment = assignmentRepository.findByDriverIdAndStatus(driverId, Status.ACTIVE)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("FP_ASSIGNMENT_NOT_FOUND", "driverId",
                        "No active vehicle assignment found for driver: " + driverId)));
        return DriverVehicleAssignmentUtil.mapToDto(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverVehicleAssignmentDto> getAssignmentHistory(String vehicleId, String driverId, Status status) {
        Specification<DriverVehicleAssignment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (vehicleId != null && !vehicleId.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("vehicleId"), vehicleId.trim()));
            }
            if (driverId != null && !driverId.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("driverId"), driverId.trim()));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<DriverVehicleAssignment> assignments = assignmentRepository.findAll(spec);
        return assignments.stream().map(DriverVehicleAssignmentUtil::mapToDto).collect(Collectors.toList());
    }
}
