package com.example.notificationservice.dto;

import com.example.notificationservice.entities.NotificationChannel;
import com.example.notificationservice.entities.RecipientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private String recipientId;
    private RecipientType recipientType;
    @Builder.Default
    private NotificationChannel channel = NotificationChannel.PUSH;
    private String eventType;
    private String referenceId;
    private String title;
    private String body;
    private Map<String, String> data;
}
