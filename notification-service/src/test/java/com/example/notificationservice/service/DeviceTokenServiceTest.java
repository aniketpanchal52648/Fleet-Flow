package com.example.notificationservice.service;

import com.example.notificationservice.dto.DeviceRegistrationRequestDto;
import com.example.notificationservice.dto.DeviceResponseDto;
import com.example.notificationservice.entities.DeviceToken;
import com.example.notificationservice.entities.RecipientType;
import com.example.notificationservice.repository.DeviceTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    private DeviceTokenService deviceTokenService;

    @BeforeEach
    void setUp() {
        deviceTokenService = new DeviceTokenService(deviceTokenRepository);
    }

    @Test
    @DisplayName("Should register a new device token and deactivate previous tokens for same recipient")
    void testRegisterDeviceSuccess() {
        DeviceRegistrationRequestDto request = DeviceRegistrationRequestDto.builder()
                .recipientId("driver-01")
                .recipientType(RecipientType.DRIVER)
                .deviceToken("new-fcm-token-123")
                .deviceType("ANDROID")
                .build();

        DeviceToken oldToken = DeviceToken.builder()
                .id("old-id")
                .recipientId("driver-01")
                .recipientType(RecipientType.DRIVER)
                .deviceToken("old-fcm-token")
                .deviceType("ANDROID")
                .isActive(true)
                .build();

        when(deviceTokenRepository.findAllByRecipientIdAndRecipientType("driver-01", RecipientType.DRIVER))
                .thenReturn(List.of(oldToken));

        when(deviceTokenRepository.findByDeviceToken("new-fcm-token-123"))
                .thenReturn(Optional.empty());

        when(deviceTokenRepository.save(any(DeviceToken.class)))
                .thenAnswer(inv -> {
                    DeviceToken dt = inv.getArgument(0);
                    if (dt.getId() == null) dt.setId("new-saved-id");
                    return dt;
                });

        DeviceResponseDto response = deviceTokenService.registerDevice(request);

        assertNotNull(response);
        assertEquals("driver-01", response.getRecipientId());
        assertEquals("new-fcm-token-123", response.getDeviceToken());
        assertTrue(response.getIsActive());

        // Verify old token was deactivated
        assertFalse(oldToken.getIsActive());
        verify(deviceTokenRepository, atLeast(2)).save(any(DeviceToken.class));
    }

    @Test
    @DisplayName("Should update existing device token if same token string is re-registered")
    void testRegisterExistingToken() {
        DeviceRegistrationRequestDto request = DeviceRegistrationRequestDto.builder()
                .recipientId("driver-02")
                .recipientType(RecipientType.DRIVER)
                .deviceToken("existing-token-xyz")
                .deviceType("IOS")
                .build();

        DeviceToken existingToken = DeviceToken.builder()
                .id("existing-id")
                .recipientId("driver-previous")
                .recipientType(RecipientType.DRIVER)
                .deviceToken("existing-token-xyz")
                .deviceType("ANDROID")
                .isActive(false)
                .build();

        when(deviceTokenRepository.findAllByRecipientIdAndRecipientType("driver-02", RecipientType.DRIVER))
                .thenReturn(List.of());

        when(deviceTokenRepository.findByDeviceToken("existing-token-xyz"))
                .thenReturn(Optional.of(existingToken));

        when(deviceTokenRepository.save(any(DeviceToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DeviceResponseDto response = deviceTokenService.registerDevice(request);

        assertNotNull(response);
        assertEquals("driver-02", response.getRecipientId());
        assertEquals("IOS", response.getDeviceType());
        assertTrue(response.getIsActive());
    }

    @Test
    @DisplayName("Should unregister active device tokens for a recipient")
    void testUnregisterDevice() {
        DeviceToken activeToken = DeviceToken.builder()
                .id("tok-1")
                .recipientId("driver-01")
                .recipientType(RecipientType.DRIVER)
                .deviceToken("token-123")
                .deviceType("ANDROID")
                .isActive(true)
                .build();

        when(deviceTokenRepository.findAllByRecipientIdAndRecipientType("driver-01", RecipientType.DRIVER))
                .thenReturn(List.of(activeToken));

        deviceTokenService.unregisterDevice("driver-01", RecipientType.DRIVER);

        assertFalse(activeToken.getIsActive());
        verify(deviceTokenRepository, times(1)).save(activeToken);
    }
}
