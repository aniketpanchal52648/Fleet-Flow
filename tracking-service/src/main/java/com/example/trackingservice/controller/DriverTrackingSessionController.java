package com.example.trackingservice.controller;

import com.example.trackingservice.dto.DriverPresenceDto;
import com.example.trackingservice.dto.StartSessionRequestDto;
import com.example.trackingservice.dto.StopSessionRequestDto;
import com.example.trackingservice.service.TrackingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/web/v1/tracking-service")
@RequiredArgsConstructor
public class DriverTrackingSessionController {

    private final TrackingSessionService sessionService;

    @PostMapping("/driver-tracking-sessions")
    public ResponseEntity<DriverPresenceDto> startSession(@Valid @RequestBody StartSessionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.startSession(request));
    }

    @PostMapping("/driver-tracking-sessions/current/stop")
    public ResponseEntity<DriverPresenceDto> stopSession(@Valid @RequestBody StopSessionRequestDto request) {
        return ResponseEntity.ok(sessionService.stopSession(request));
    }

    @GetMapping("/drivers/{driverId}/presence")
    public ResponseEntity<DriverPresenceDto> getPresence(@PathVariable String driverId) {
        return ResponseEntity.ok(sessionService.getPresence(driverId));
    }
}
