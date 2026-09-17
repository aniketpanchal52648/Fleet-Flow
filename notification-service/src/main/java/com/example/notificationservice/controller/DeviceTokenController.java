package com.example.notificationservice.controller;

import com.example.notificationservice.dto.DeviceRegistrationRequestDto;
import com.example.notificationservice.dto.DeviceResponseDto;
import com.example.notificationservice.entities.RecipientType;
import com.example.notificationservice.service.DeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/web/v1/notification-service/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping("/register")
    public ResponseEntity<DeviceResponseDto> registerDevice(@Valid @RequestBody DeviceRegistrationRequestDto request) {
        log.info("REST: Registering device token for recipient [{}]", request.getRecipientId());
        DeviceResponseDto response = deviceTokenService.registerDevice(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<Void> unregisterDevice(
            @RequestParam String recipientId,
            @RequestParam(defaultValue = "DRIVER") RecipientType recipientType) {
        log.info("REST: Unregistering devices for recipient [{}] ({})", recipientId, recipientType);
        deviceTokenService.unregisterDevice(recipientId, recipientType);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<DeviceResponseDto> getActiveDevice(
            @RequestParam String recipientId,
            @RequestParam(defaultValue = "DRIVER") RecipientType recipientType) {
        log.info("REST: Fetching active device for recipient [{}] ({})", recipientId, recipientType);
        return deviceTokenService.getActiveDevice(recipientId, recipientType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
