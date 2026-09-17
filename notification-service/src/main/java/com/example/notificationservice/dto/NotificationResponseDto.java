package com.example.notificationservice.dto;

import com.example.notificationservice.entities.NotificationChannel;
import com.example.notificationservice.entities.NotificationStatus;
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
public class NotificationResponseDto {
    private String id;
    private String recipientId;
    private RecipientType recipientType;
    private NotificationChannel channel;
    private String eventType;
    private String referenceId;
    private String title;
    private String body;
    private String payload;
    private NotificationStatus status;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
