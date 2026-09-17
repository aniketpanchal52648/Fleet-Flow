package com.example.shipmentservice.service;

import com.example.shipmentservice.dto.ApiResponseDto;
import com.example.shipmentservice.dto.ShipmentStatusUpdateDto;
import com.example.shipmentservice.dto.UserShipmentCollection;
import com.example.shipmentservice.dto.UserShipmentDto;
import com.example.shipmentservice.entities.OutboxEvent;
import com.example.shipmentservice.entities.OutboxStatus;
import com.example.shipmentservice.entities.Shipment;
import com.example.shipmentservice.entities.ShipmentStatus;
import com.example.shipmentservice.repository.OutboxEventRepository;
import com.example.shipmentservice.repository.ShipmentRepository;
import com.example.shipmentservice.repository.spec.ShipmentSpecification;
import com.example.shipmentservice.util.ShipmentServiceUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

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
        // --- TRANSACTIONAL OUTBOX: Save event in the same DB transaction ---
        saveShipmentCreatedOutboxEvent(created);
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

    private void saveShipmentCreatedOutboxEvent(Shipment shipment) {
        try {
            org.example.event.AddressEventDto pickupDto = shipment.getPickupLocation() != null ?
                    org.example.event.AddressEventDto.builder()
                            .addressLineOne(shipment.getPickupLocation().getAddressLineOne())
                            .addressLineTwo(shipment.getPickupLocation().getAddressLineTwo())
                            .district(shipment.getPickupLocation().getDistrict())
                            .state(shipment.getPickupLocation().getState())
                            .pincode(shipment.getPickupLocation().getPincode())
                            .geoLocation(shipment.getPickupLocation().getGeoLocation())
                            .addressType(shipment.getPickupLocation().getAddressType() != null ? shipment.getPickupLocation().getAddressType().name() : null)
                            .build() : null;
            org.example.event.AddressEventDto dropDto = shipment.getDropLocation() != null ?
                    org.example.event.AddressEventDto.builder()
                            .addressLineOne(shipment.getDropLocation().getAddressLineOne())
                            .addressLineTwo(shipment.getDropLocation().getAddressLineTwo())
                            .district(shipment.getDropLocation().getDistrict())
                            .state(shipment.getDropLocation().getState())
                            .pincode(shipment.getDropLocation().getPincode())
                            .geoLocation(shipment.getDropLocation().getGeoLocation())
                            .addressType(shipment.getDropLocation().getAddressType() != null ? shipment.getDropLocation().getAddressType().name() : null)
                            .build() : null;
            org.example.event.ShipmentCreatedEvent event = org.example.event.ShipmentCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("SHIPMENT_CREATED")
                    .shipmentId(shipment.getShipmentId())
                    .userId(shipment.getUserId())
                    .vehicleType(shipment.getVehicleType())
                    .weightKg(shipment.getWeightKg())
                    .pickupAddress(pickupDto)
                    .dropAddress(dropDto)
                    .status(shipment.getShipmentStatus().name())
                    .createdAt(LocalDateTime.now())
                    .build();
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("SHIPMENT")
                    .aggregateId(shipment.getShipmentId())
                    .eventType("SHIPMENT_CREATED")
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
            outboxEventRepository.save(outboxEvent);
            log.info("Saved outbox event for shipmentId: {}", shipment.getShipmentId());
        } catch (Exception e) {
            log.error("Failed to serialize and save outbox event for shipment: {}", shipment.getShipmentId(), e);
            throw new RuntimeException("Could not create shipment outbox event", e);
        }
    }
}
