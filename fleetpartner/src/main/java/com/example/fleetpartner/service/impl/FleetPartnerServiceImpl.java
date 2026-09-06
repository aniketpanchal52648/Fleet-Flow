package com.example.fleetpartner.service.impl;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.FleetPartnerDto;
import com.example.fleetpartner.dto.VehicleDto;
import com.example.fleetpartner.entites.Availability;
import com.example.fleetpartner.entites.FleetPartner;
import com.example.fleetpartner.entites.Status;
import com.example.fleetpartner.entites.Vehicle;
import com.example.fleetpartner.repository.FleetPartnerRepository;
import com.example.fleetpartner.repository.VehicleRepository;
import com.example.fleetpartner.service.FleetPartnerService;
import com.example.fleetpartner.util.VehicleUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorDetail;
import org.example.error.ErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FleetPartnerServiceImpl implements FleetPartnerService {

    private final FleetPartnerRepository fleetPartnerRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public FleetPartnerDto createPartner(FleetPartnerDto partnerDto) {
        if (partnerDto.getPartnerId() == null || partnerDto.getPartnerId().trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("FP_PARTNER_ID_REQUIRED", "partnerId", "partnerId is required"));
        }

        if (fleetPartnerRepository.existsByPartnerId(partnerDto.getPartnerId())) {
            throw new ErrorException(new ErrorDetail("FP_ALREADY_EXISTS", "partnerId",
                    "Fleet partner already exists with partnerId: " + partnerDto.getPartnerId()));
        }

        FleetPartner partner = new FleetPartner();
        partner.setPartnerId(partnerDto.getPartnerId());
        partner.setPartnerType(partnerDto.getPartnerType());
        partner.setName(partnerDto.getName());
        partner.setOwnerName(partnerDto.getOwnerName());
        partner.setKeycloakId(partnerDto.getKeycloakId());
        partner.setStatus(partnerDto.getStatus() != null ? partnerDto.getStatus() : Status.PENDING);

        if (partnerDto.getTags() != null) {
            partner.setTags(new ArrayList<>(partnerDto.getTags()));
        }

        FleetPartner savedPartner = fleetPartnerRepository.save(partner);

        if (partnerDto.getVehicles() != null && !partnerDto.getVehicles().isEmpty()) {
            List<Vehicle> vehicles = mapVehicleDtosToEntities(partnerDto.getVehicles(), savedPartner);
            List<Vehicle> savedVehicles = vehicleRepository.saveAll(vehicles);
            savedPartner.setVehicles(savedVehicles);
        }

        log.info("Created new Fleet Partner with partnerId: {}", savedPartner.getPartnerId());
        return mapToDto(savedPartner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FleetPartnerDto> getAllPartners(Status status) {
        List<FleetPartner> partners;
        if (status != null) {
            partners = fleetPartnerRepository.findByStatus(status);
        } else {
            partners = fleetPartnerRepository.findByStatus(Status.ACTIVE);
        }
        return partners.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FleetPartnerDto getPartnerByPartnerId(String partnerId) {
        FleetPartner partner = findPartnerOrThrow(partnerId);
        return mapToDto(partner);
    }

    @Override
    public FleetPartnerDto updatePartner(String partnerId, FleetPartnerDto partnerDto) {
        FleetPartner partner = findPartnerOrThrow(partnerId);

        if (partnerDto.getName() != null) {
            partner.setName(partnerDto.getName());
        }
        if (partnerDto.getOwnerName() != null) {
            partner.setOwnerName(partnerDto.getOwnerName());
        }
        if (partnerDto.getPartnerType() != null) {
            partner.setPartnerType(partnerDto.getPartnerType());
        }
        if (partnerDto.getKeycloakId() != null) {
            partner.setKeycloakId(partnerDto.getKeycloakId());
        }
        if (partnerDto.getTags() != null) {
            partner.getTags().clear();
            partner.getTags().addAll(partnerDto.getTags());
        }

        FleetPartner updated = fleetPartnerRepository.save(partner);
        log.info("Updated Fleet Partner with partnerId: {}", partnerId);
        return mapToDto(updated);
    }

    @Override
    public ApiResponseDto activatePartner(String partnerId) {
        FleetPartner partner = findPartnerOrThrow(partnerId);
        partner.setStatus(Status.ACTIVE);
        fleetPartnerRepository.save(partner);
        log.info("Activated Fleet Partner with partnerId: {}", partnerId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Fleet partner activated successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponseDto inactivatePartner(String partnerId) {
        FleetPartner partner = findPartnerOrThrow(partnerId);
        partner.setStatus(Status.INACTIVE);
        fleetPartnerRepository.save(partner);
        log.info("Inactivated Fleet Partner with partnerId: {}", partnerId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Fleet partner set to inactive successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponseDto deletePartner(String partnerId) {
        FleetPartner partner = findPartnerOrThrow(partnerId);
        partner.setStatus(Status.DELETED);
        fleetPartnerRepository.save(partner);
        log.info("Soft deleted Fleet Partner with partnerId: {}", partnerId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Fleet partner deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponseDto associateVehicles(String partnerId, List<String> vehicleIds) {
        FleetPartner partner = findPartnerOrThrow(partnerId);
        if (vehicleIds == null || vehicleIds.isEmpty()) {
            throw new ErrorException(new ErrorDetail("FP_INVALID_REQUEST", "vehicleIds",
                    "Vehicle IDs list cannot be empty"));
        }

        List<Vehicle> vehicles = vehicleRepository.findByVehicleIdIn(vehicleIds);
        if (vehicles.isEmpty()) {
            throw new ErrorException(new ErrorDetail("FP_VEHICLE_NOT_FOUND", "vehicleIds",
                    "No matching vehicles found for the provided IDs"));
        }

        for (Vehicle vehicle : vehicles) {
            vehicle.setFleetPartner(partner);
        }
        vehicleRepository.saveAll(vehicles);
        log.info("Associated {} vehicle(s) to partner {}", vehicles.size(), partnerId);

        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Successfully associated " + vehicles.size() + " vehicle(s) to partner " + partnerId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponseDto disassociateVehicles(String partnerId, List<String> vehicleIds) {
        findPartnerOrThrow(partnerId);
        if (vehicleIds == null || vehicleIds.isEmpty()) {
            throw new ErrorException(new ErrorDetail("FP_INVALID_REQUEST", "vehicleIds",
                    "Vehicle IDs list cannot be empty"));
        }

        List<Vehicle> vehicles = vehicleRepository.findByVehicleIdIn(vehicleIds);
        int disassociatedCount = 0;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getFleetPartner() != null && partnerId.equals(vehicle.getFleetPartner().getPartnerId())) {
                vehicle.setFleetPartner(null);
                disassociatedCount++;
            }
        }
        vehicleRepository.saveAll(vehicles);
        log.info("Disassociated {} vehicle(s) from partner {}", disassociatedCount, partnerId);

        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Successfully disassociated " + disassociatedCount + " vehicle(s) from partner " + partnerId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private FleetPartner findPartnerOrThrow(String partnerId) {
        return fleetPartnerRepository.findByPartnerId(partnerId)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("FP_NOT_FOUND", "partnerId",
                        "Fleet partner not found with partnerId: " + partnerId)));
    }

    private List<Vehicle> mapVehicleDtosToEntities(List<VehicleDto> vehicleDtos, FleetPartner partner) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (VehicleDto vDto : vehicleDtos) {
            Vehicle vehicle = VehicleUtil.mapToEntity(vDto);
            if (vehicle.getVehicleId() == null || vehicle.getVehicleId().trim().isEmpty()) {
                vehicle.setVehicleId("VEH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }
            vehicle.setFleetPartner(partner);
            vehicles.add(vehicle);
        }
        return vehicles;
    }

    private FleetPartnerDto mapToDto(FleetPartner partner) {
        List<VehicleDto> vehicleDtos = new ArrayList<>();
        if (partner.getVehicles() != null) {
            vehicleDtos = partner.getVehicles().stream()
                    .map(VehicleUtil::mapToDto)
                    .collect(Collectors.toList());
        }

        return FleetPartnerDto.builder()
                .id(partner.getId())
                .partnerId(partner.getPartnerId())
                .partnerType(partner.getPartnerType())
                .name(partner.getName())
                .ownerName(partner.getOwnerName())
                .keycloakId(partner.getKeycloakId())
                .status(partner.getStatus())
                .tags(partner.getTags() != null ? new ArrayList<>(partner.getTags()) : new ArrayList<>())
                .vehicles(vehicleDtos)
                .build();
    }
}
