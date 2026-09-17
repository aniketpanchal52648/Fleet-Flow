package com.example.trackingservice.controller;

import com.example.trackingservice.dto.DriverLocationEventDto;
import com.example.trackingservice.service.DriverLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/web/v1/tracking-service")
@RequiredArgsConstructor
public class DriverLocationController {

    private final DriverLocationService locationService;

    @PostMapping("/driver-locations")
    public ResponseEntity<Void> sendLocation(@Valid @RequestBody DriverLocationEventDto event) {
        locationService.recordLocation(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
