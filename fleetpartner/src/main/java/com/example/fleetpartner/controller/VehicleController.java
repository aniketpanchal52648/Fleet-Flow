package com.example.fleetpartner.controller;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.VehicleDto;
import com.example.fleetpartner.entites.Status;
import com.example.fleetpartner.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("web/v1/fleet-partner-service/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleDto> createVehicle(@RequestBody VehicleDto vehicleDto) {
        VehicleDto created = vehicleService.createVehicle(vehicleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<VehicleDto>> getVehicles(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false, name = "fleetpartner") String fleetPartner,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) Double capacity,
            @RequestParam(required = false) String dimension
    ) {
        List<VehicleDto> vehicles = vehicleService.getVehicles(status, fleetPartner, vehicleType, capacity, dimension);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleDto> getVehicleById(@PathVariable String vehicleId) {
        return ResponseEntity.ok(vehicleService.getVehicleByVehicleId(vehicleId));
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<VehicleDto> updateVehicle(
            @PathVariable String vehicleId,
            @RequestBody VehicleDto vehicleDto) {
        return ResponseEntity.ok(vehicleService.updateVehicle(vehicleId, vehicleDto));
    }

    @PatchMapping("/{vehicleId}/delete")
    public ResponseEntity<ApiResponseDto> deleteVehicle(@PathVariable String vehicleId) {
        return ResponseEntity.ok(vehicleService.deleteVehicle(vehicleId));
    }

    @PatchMapping("/{vehicleId}/activate")
    public ResponseEntity<ApiResponseDto> activateVehicle(@PathVariable String vehicleId) {
        return ResponseEntity.ok(vehicleService.activateVehicle(vehicleId));
    }

    @PatchMapping("/{vehicleId}/inactivate")
    public ResponseEntity<ApiResponseDto> inactivateVehicle(@PathVariable String vehicleId) {
        return ResponseEntity.ok(vehicleService.inactivateVehicle(vehicleId));
    }
}
