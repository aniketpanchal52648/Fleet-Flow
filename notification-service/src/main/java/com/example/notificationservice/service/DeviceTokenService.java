package com.example.notificationservice.service;

import com.example.notificationservice.dto.DeviceRegistrationRequestDto;
import com.example.notificationservice.dto.DeviceResponseDto;
import com.example.notificationservice.entities.DeviceToken;
import com.example.notificationservice.entities.RecipientType;
import com.example.notificationservice.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public DeviceResponseDto registerDevice(DeviceRegistrationRequestDto request) {
        log.info("Registering device token for recipient [{}] ({}) on device type [{}]",
                request.getRecipientId(), request.getRecipientType(), request.getDeviceType());

        // 1. Deactivate existing active tokens for this recipient (single active token per recipient)
        List<DeviceToken> existingRecipientTokens = deviceTokenRepository.findAllByRecipientIdAndRecipientType(
                request.getRecipientId(), request.getRecipientType());
        for (DeviceToken dt : existingRecipientTokens) {
            if (Boolean.TRUE.equals(dt.getIsActive())) {
                dt.setIsActive(false);
                deviceTokenRepository.save(dt);
            }
        }

        // 2. Check if this exact token string already exists (e.g. previously registered or re-assigned)
        Optional<DeviceToken> existingTokenOpt = deviceTokenRepository.findByDeviceToken(request.getDeviceToken());
        DeviceToken deviceToken;
        if (existingTokenOpt.isPresent()) {
            deviceToken = existingTokenOpt.get();
            deviceToken.setRecipientId(request.getRecipientId());
            deviceToken.setRecipientType(request.getRecipientType());
            deviceToken.setDeviceType(request.getDeviceType());
            deviceToken.setIsActive(true);
        } else {
            deviceToken = DeviceToken.builder()
                    .recipientId(request.getRecipientId())
                    .recipientType(request.getRecipientType())
                    .deviceToken(request.getDeviceToken())
                    .deviceType(request.getDeviceType())
                    .isActive(true)
                    .build();
        }

        DeviceToken saved = deviceTokenRepository.save(deviceToken);
        log.info("Saved active device token with ID [{}] for recipient [{}]", saved.getId(), saved.getRecipientId());
        return mapToDto(saved);
    }

    @Transactional
    public void unregisterDevice(String recipientId, RecipientType recipientType) {
        log.info("Unregistering devices for recipient [{}] ({})", recipientId, recipientType);
        List<DeviceToken> tokens = deviceTokenRepository.findAllByRecipientIdAndRecipientType(recipientId, recipientType);
        for (DeviceToken dt : tokens) {
            if (Boolean.TRUE.equals(dt.getIsActive())) {
                dt.setIsActive(false);
                deviceTokenRepository.save(dt);
                log.info("Deactivated device token [{}] for recipient [{}]", dt.getId(), recipientId);
            }
        }
    }

    public Optional<DeviceResponseDto> getActiveDevice(String recipientId, RecipientType recipientType) {
        return deviceTokenRepository.findByRecipientIdAndRecipientTypeAndIsActiveTrue(recipientId, recipientType)
                .map(this::mapToDto);
    }

    public DeviceResponseDto mapToDto(DeviceToken entity) {
        return DeviceResponseDto.builder()
                .id(entity.getId())
                .recipientId(entity.getRecipientId())
                .recipientType(entity.getRecipientType())
                .deviceToken(entity.getDeviceToken())
                .deviceType(entity.getDeviceType())
                .isActive(entity.getIsActive())
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : entity.getCreatedAt())
                .build();
    }
}
