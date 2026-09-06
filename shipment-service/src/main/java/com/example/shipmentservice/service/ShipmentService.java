package com.example.shipmentservice.service;

import com.example.shipmentservice.dto.ApiResponseDto;
import com.example.shipmentservice.dto.ShipmentStatusUpdateDto;
import com.example.shipmentservice.dto.UserShipmentCollection;
import com.example.shipmentservice.dto.UserShipmentDto;
import com.example.shipmentservice.entities.Shipment;
import com.example.shipmentservice.entities.ShipmentStatus;
import com.example.shipmentservice.repository.ShipmentRepository;
import com.example.shipmentservice.repository.spec.ShipmentSpecification;
import com.example.shipmentservice.util.ShipmentServiceUtils;
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
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final UserValidationService userValidationService;

    public UserShipmentDto createShipment(UserShipmentDto userShipmentDto) {
        String userId = userShipmentDto.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("USER_ID_REQUIRED", "userId", "userId is required"));
        }

        if (!userValidationService.validateUserById(userId.trim())) {
            throw new ErrorException(new ErrorDetail("USER_NOT_FOUND", "userId", "User with given Id not found: " + userId));
        }

        Shipment shipment = ShipmentServiceUtils.dtoToEntity(userShipmentDto);
        if (shipment.getShipmentId() == null || shipment.getShipmentId().trim().isEmpty()) {
            shipment.setShipmentId("SHP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (shipment.getShipmentStatus() == null) {
            shipment.setShipmentStatus(ShipmentStatus.CREATED);
        }

        Shipment created = shipmentRepository.save(shipment);
        log.info("Created shipment with shipmentId: {}", created.getShipmentId());
        return ShipmentServiceUtils.entityToDto(created);
    }

    @Transactional(readOnly = true)
    public UserShipmentDto getShipment(String shipmentId) {
        Shipment shipment = findShipmentOrThrow(shipmentId);
        return ShipmentServiceUtils.entityToDto(shipment);
    }

    @Transactional(readOnly = true)
    public List<UserShipmentDto> getShipments(
            ShipmentStatus status,
            String userId,
            String driverId,
            String vehicleId,
            String vehicleType
    ) {
        Specification<Shipment> spec = ShipmentSpecification.filterShipments(status, userId, driverId, vehicleId, vehicleType);
        List<Shipment> shipments = shipmentRepository.findAll(spec);
        return shipments.stream().map(ShipmentServiceUtils::entityToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserShipmentCollection getShipmentByUserId(String userId) {
        if (!userValidationService.validateUserById(userId)) {
            throw new ErrorException(new ErrorDetail("USER_NOT_FOUND", "userId", "User with given Id not found: " + userId));
        }
        List<Shipment> shipments = shipmentRepository.findByUserId(userId);
        List<UserShipmentDto> shipmentsDto = ShipmentServiceUtils.entityToDtoList(shipments);
        UserShipmentCollection collection = new UserShipmentCollection();
        collection.setData(shipmentsDto);
        return collection;
    }

    public UserShipmentDto updateShipment(String shipmentId, UserShipmentDto dto) {
        Shipment shipment = findShipmentOrThrow(shipmentId);

        if (shipment.getShipmentStatus() != ShipmentStatus.CREATED
                && shipment.getShipmentStatus() != ShipmentStatus.AWAITING_DRIVER) {
            throw new ErrorException(new ErrorDetail("SHIPMENT_CANNOT_BE_UPDATED", "shipmentStatus",
                    "Shipment cannot be updated once driver is assigned or trip has started. Current status: "
                            + shipment.getShipmentStatus()));
        }

        ShipmentServiceUtils.copyDtoToEntity(dto, shipment);
        Shipment updated = shipmentRepository.save(shipment);
        log.info("Updated shipment details for shipmentId: {}", shipmentId);
        return ShipmentServiceUtils.entityToDto(updated);
    }

    public ApiResponseDto updateShipmentStatus(String shipmentId, ShipmentStatusUpdateDto statusDto) {
        if (statusDto.getStatus() == null) {
            throw new ErrorException(new ErrorDetail("STATUS_REQUIRED", "status", "Target status is required"));
        }

        Shipment shipment = findShipmentOrThrow(shipmentId);
        ShipmentStatus currentStatus = shipment.getShipmentStatus();
        ShipmentStatus targetStatus = statusDto.getStatus();

        if (currentStatus == ShipmentStatus.COMPLETED || currentStatus == ShipmentStatus.CANCELLED) {
            throw new ErrorException(new ErrorDetail("INVALID_STATUS_TRANSITION", "shipmentStatus",
                    "Cannot transition status from a terminal state (" + currentStatus + ")"));
        }

        if (targetStatus == ShipmentStatus.DRIVER_ASSIGNED) {
            if (statusDto.getDriverId() != null && !statusDto.getDriverId().trim().isEmpty()) {
                shipment.setDriverId(statusDto.getDriverId().trim());
            }
            if (statusDto.getVehicleId() != null && !statusDto.getVehicleId().trim().isEmpty()) {
                shipment.setVehicleId(statusDto.getVehicleId().trim());
            }
            if (shipment.getDriverId() == null || shipment.getDriverId().trim().isEmpty()) {
                throw new ErrorException(new ErrorDetail("DRIVER_ID_REQUIRED", "driverId",
                        "driverId is required when transitioning to DRIVER_ASSIGNED"));
            }
        } else if (targetStatus == ShipmentStatus.PICKED_UP) {
            shipment.setPickupTime(LocalDateTime.now());
        } else if (targetStatus == ShipmentStatus.COMPLETED) {
            shipment.setDeliveryTime(LocalDateTime.now());
        } else if (targetStatus == ShipmentStatus.CANCELLED) {
            if (statusDto.getCancellationReason() != null && !statusDto.getCancellationReason().trim().isEmpty()) {
                shipment.setCancellationReason(statusDto.getCancellationReason().trim());
            }
        }

        shipment.setShipmentStatus(targetStatus);
        shipmentRepository.save(shipment);
        log.info("Transitioned shipmentId: {} from {} to {}", shipmentId, currentStatus, targetStatus);

        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Shipment status updated to " + targetStatus)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private Shipment findShipmentOrThrow(String shipmentId) {
        return shipmentRepository.findByShipmentId(shipmentId)
                .or(() -> shipmentRepository.findById(shipmentId))
                .orElseThrow(() -> new ErrorException(new ErrorDetail("SHIPMENT_NOT_FOUND", "shipmentId",
                        "Shipment not found with id: " + shipmentId)));
    }
}
