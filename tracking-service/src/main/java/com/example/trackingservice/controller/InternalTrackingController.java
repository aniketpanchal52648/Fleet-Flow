package com.example.trackingservice.controller;

import com.example.trackingservice.dto.NearbyDriverResponseDto;
import com.example.trackingservice.service.NearbyDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/tracking-service")
@RequiredArgsConstructor
public class InternalTrackingController {

    private final NearbyDriverService nearbyDriverService;

    @GetMapping("/nearby-drivers")
    public ResponseEntity<List<NearbyDriverResponseDto>> getNearbyDrivers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm
    ) {
        return ResponseEntity.ok(nearbyDriverService.findNearbyDrivers(latitude, longitude, radiusKm));
    }
}
