package com.example.notificationservice.dto;

import com.example.notificationservice.entities.RecipientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceResponseDto {
    private String id;
    private String recipientId;
    private RecipientType recipientType;
    private String deviceToken;
    private String deviceType;
    private Boolean isActive;
    private LocalDateTime updatedAt;
}
