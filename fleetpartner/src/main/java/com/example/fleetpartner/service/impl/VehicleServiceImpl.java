package com.example.fleetpartner.service.impl;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.VehicleDto;
import com.example.fleetpartner.entites.FleetPartner;
import com.example.fleetpartner.entites.Status;
import com.example.fleetpartner.entites.Vehicle;
import com.example.fleetpartner.repository.FleetPartnerRepository;
import com.example.fleetpartner.repository.VehicleRepository;
import com.example.fleetpartner.repository.spec.VehicleSpecification;
import com.example.fleetpartner.service.VehicleService;
import com.example.fleetpartner.util.VehicleUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorDetail;
import org.example.error.ErrorException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final FleetPartnerRepository fleetPartnerRepository;

    @Override
    public VehicleDto createVehicle(VehicleDto vehicleDto) {
        // 1. Validation first
        if (vehicleDto.getRegistrationNo() == null || vehicleDto.getRegistrationNo().trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("FP_REGISTRATION_NO_REQUIRED", "registrationNo",
                    "Vehicle registration number is required"));
        }

        if (vehicleRepository.existsByRegistrationNo(vehicleDto.getRegistrationNo().trim())) {
            throw new ErrorException(new ErrorDetail("FP_REGISTRATION_NO_EXISTS", "registrationNo",
                    "Vehicle with registration number already exists: " + vehicleDto.getRegistrationNo()));
        }

        FleetPartner partner = null;
        if (vehicleDto.getFleetPartnerId() != null && !vehicleDto.getFleetPartnerId().trim().isEmpty()) {
            partner = fleetPartnerRepository.findByPartnerId(vehicleDto.getFleetPartnerId().trim())
                    .orElseThrow(() -> new ErrorException(new ErrorDetail("FP_NOT_FOUND", "fleetPartnerId",
                            "Fleet partner not found with partnerId: " + vehicleDto.getFleetPartnerId())));
        }

        // 2. Map DTO to Entity using utility
        Vehicle vehicle = VehicleUtil.mapToEntity(vehicleDto);
        if (vehicle.getVehicleId() == null || vehicle.getVehicleId().trim().isEmpty()) {
            vehicle.setVehicleId("VEH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (partner != null) {
            vehicle.setFleetPartner(partner);
        }

        // 3. Save entity
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Created vehicle with vehicleId: {}", saved.getVehicleId());
        return VehicleUtil.mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleDto> getVehicles(Status status, String fleetPartnerId, String vehicleType, Double capacity, String dimension) {
        Specification<Vehicle> spec = VehicleSpecification.filterVehicles(status, fleetPartnerId, vehicleType, capacity, dimension);
        List<Vehicle> vehicles = vehicleRepository.findAll(spec);
        return vehicles.stream().map(VehicleUtil::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleDto getVehicleByVehicleId(String vehicleId) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);
        return VehicleUtil.mapToDto(vehicle);
    }

    @Override
    public VehicleDto updateVehicle(String vehicleId, VehicleDto vehicleDto) {
        // 1. Validation first
        Vehicle vehicle = findVehicleOrThrow(vehicleId);

        if (vehicleDto.getRegistrationNo() != null && !vehicleDto.getRegistrationNo().trim().isEmpty()) {
            String newRegNo = vehicleDto.getRegistrationNo().trim();
            if (!newRegNo.equalsIgnoreCase(vehicle.getRegistrationNo())) {
                if (vehicleRepository.existsByRegistrationNo(newRegNo)) {
                    throw new ErrorException(new ErrorDetail("FP_REGISTRATION_NO_EXISTS", "registrationNo",
                            "Vehicle with registration number already exists: " + newRegNo));
                }
            }
        }

        FleetPartner partner = null;
        boolean updatePartnerRef = false;
        if (vehicleDto.getFleetPartnerId() != null) {
            updatePartnerRef = true;
            if (!vehicleDto.getFleetPartnerId().trim().isEmpty()) {
                partner = fleetPartnerRepository.findByPartnerId(vehicleDto.getFleetPartnerId().trim())
                        .orElseThrow(() -> new ErrorException(new ErrorDetail("FP_NOT_FOUND", "fleetPartnerId",
                                "Fleet partner not found with partnerId: " + vehicleDto.getFleetPartnerId())));
            }
        }

        // 2. Map updates via utility
        VehicleUtil.copyDtoToEntity(vehicleDto, vehicle);
        if (updatePartnerRef) {
            vehicle.setFleetPartner(partner);
        }

        // 3. Save entity
        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("Updated vehicle with vehicleId: {}", vehicleId);
        return VehicleUtil.mapToDto(updated);
    }

    @Override
    public ApiResponseDto deleteVehicle(String vehicleId) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);
        vehicle.setStatus(Status.DELETED);
        vehicleRepository.save(vehicle);
        log.info("Soft deleted vehicle with vehicleId: {}", vehicleId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Vehicle deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponseDto activateVehicle(String vehicleId) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);
        vehicle.setStatus(Status.ACTIVE);
        vehicleRepository.save(vehicle);
        log.info("Activated vehicle with vehicleId: {}", vehicleId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Vehicle activated successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponseDto inactivateVehicle(String vehicleId) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);
        vehicle.setStatus(Status.INACTIVE);
        vehicleRepository.save(vehicle);
        log.info("Inactivated vehicle with vehicleId: {}", vehicleId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Vehicle set to inactive successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private Vehicle findVehicleOrThrow(String vehicleId) {
        return vehicleRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("FP_VEHICLE_NOT_FOUND", "vehicleId",
                        "Vehicle not found with vehicleId: " + vehicleId)));
    }
}
