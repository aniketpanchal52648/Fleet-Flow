package com.example.fleetpartner.controller;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.DriverDto;
import com.example.fleetpartner.entites.Availability;
import com.example.fleetpartner.entites.Status;
import com.example.fleetpartner.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("web/v1/fleet-partner-service/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriverDto> createDriver(@RequestBody DriverDto driverDto) {
        DriverDto created = driverService.createDriver(driverDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<DriverDto>> getDrivers(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String partnerId,
            @RequestParam(required = false) Availability availability
    ) {
        List<DriverDto> drivers = driverService.getDrivers(status, partnerId, availability);
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverDto> getDriverById(@PathVariable String driverId) {
        return ResponseEntity.ok(driverService.getDriverByDriverId(driverId));
    }

    @PutMapping("/{driverId}")
    public ResponseEntity<DriverDto> updateDriver(
            @PathVariable String driverId,
            @RequestBody DriverDto driverDto) {
        return ResponseEntity.ok(driverService.updateDriver(driverId, driverDto));
    }

    @PatchMapping("/{driverId}/delete")
    public ResponseEntity<ApiResponseDto> deleteDriver(@PathVariable String driverId) {
        return ResponseEntity.ok(driverService.deleteDriver(driverId));
    }

    @PatchMapping("/{driverId}/activate")
    public ResponseEntity<ApiResponseDto> activateDriver(@PathVariable String driverId) {
        return ResponseEntity.ok(driverService.activateDriver(driverId));
    }

    @PatchMapping("/{driverId}/inactivate")
    public ResponseEntity<ApiResponseDto> inactivateDriver(@PathVariable String driverId) {
        return ResponseEntity.ok(driverService.inactivateDriver(driverId));
    }
}
