package com.example.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSendResult {
    private boolean success;
    private String messageId;
    private String errorMessage;

    public static NotificationSendResult ok(String messageId) {
        return NotificationSendResult.builder()
                .success(true)
                .messageId(messageId)
                .build();
    }

    public static NotificationSendResult failure(String errorMessage) {
        return NotificationSendResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
