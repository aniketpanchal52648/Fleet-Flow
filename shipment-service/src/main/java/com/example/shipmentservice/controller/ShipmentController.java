package com.example.shipmentservice.controller;

import com.example.shipmentservice.dto.ApiResponseDto;
import com.example.shipmentservice.dto.ShipmentStatusUpdateDto;
import com.example.shipmentservice.dto.UserShipmentCollection;
import com.example.shipmentservice.dto.UserShipmentDto;
import com.example.shipmentservice.entities.ShipmentStatus;
import com.example.shipmentservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("web/v1/shipment-service/shipment")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<UserShipmentDto> createShipment(@RequestBody UserShipmentDto userShipmentDto) {
        UserShipmentDto created = shipmentService.createShipment(userShipmentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<UserShipmentDto>> getShipments(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String driverId,
            @RequestParam(required = false) String vehicleId,
            @RequestParam(required = false) String vehicleType
    ) {
        List<UserShipmentDto> shipments = shipmentService.getShipments(status, userId, driverId, vehicleId, vehicleType);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<UserShipmentDto> getShipment(@PathVariable String shipmentId) {
        return ResponseEntity.ok(shipmentService.getShipment(shipmentId));
    }

    @PutMapping("/{shipmentId}")
    public ResponseEntity<UserShipmentDto> updateShipment(
            @PathVariable String shipmentId,
            @RequestBody UserShipmentDto userShipmentDto) {
        return ResponseEntity.ok(shipmentService.updateShipment(shipmentId, userShipmentDto));
    }

    @PatchMapping("/{shipmentId}/status")
    public ResponseEntity<ApiResponseDto> updateShipmentStatus(
            @PathVariable String shipmentId,
            @RequestBody ShipmentStatusUpdateDto statusDto) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(shipmentId, statusDto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserShipmentCollection> getShipmentsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(shipmentService.getShipmentByUserId(userId));
    }
}
