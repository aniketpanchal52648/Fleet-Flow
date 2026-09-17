package com.example.notificationservice.dto;

import com.example.notificationservice.entities.RecipientType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistrationRequestDto {

    @NotBlank(message = "recipientId is required")
    private String recipientId;

    @Builder.Default
    private RecipientType recipientType = RecipientType.DRIVER;

    @NotBlank(message = "deviceToken is required")
    private String deviceToken;

    @NotBlank(message = "deviceType is required (e.g. ANDROID, IOS, WEB)")
    private String deviceType;
}
