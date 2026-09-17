package com.example.notificationservice.service;

import com.example.notificationservice.channel.NotificationChannelSender;
import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.dto.NotificationResponseDto;
import com.example.notificationservice.dto.NotificationSendResult;
import com.example.notificationservice.entities.DeviceToken;
import com.example.notificationservice.entities.NotificationHistory;
import com.example.notificationservice.entities.NotificationStatus;
import com.example.notificationservice.repository.DeviceTokenRepository;
import com.example.notificationservice.repository.NotificationHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcherService {

    private final List<NotificationChannelSender> channelSenders;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public NotificationResponseDto dispatch(NotificationRequest request) {
        log.info("Processing notification request for recipient [{}] on channel [{}]",
                request.getRecipientId(), request.getChannel());

        String payloadJson = serializeData(request.getData());

        // 1. Strict Token Lookup: Check if recipient has an active device registered
        Optional<DeviceToken> tokenOpt = deviceTokenRepository.findByRecipientIdAndRecipientTypeAndIsActiveTrue(
                request.getRecipientId(), request.getRecipientType());

        if (tokenOpt.isEmpty()) {
            log.warn("Recipient [{}] ({}) has NO active device token registered. Recording NO_DEVICE_TOKEN.",
                    request.getRecipientId(), request.getRecipientType());

            NotificationHistory history = NotificationHistory.builder()
                    .recipientId(request.getRecipientId())
                    .recipientType(request.getRecipientType())
                    .channel(request.getChannel())
                    .eventType(request.getEventType())
                    .referenceId(request.getReferenceId())
                    .title(request.getTitle())
                    .body(request.getBody())
                    .payload(payloadJson)
                    .status(NotificationStatus.NO_DEVICE_TOKEN)
                    .errorMessage("No active device token registered for recipient [" + request.getRecipientId() + "]")
                    .build();

            NotificationHistory saved = notificationHistoryRepository.save(history);
            return mapToDto(saved);
        }

        DeviceToken deviceToken = tokenOpt.get();

        // 2. Resolve Channel Sender (Strategy Pattern)
        NotificationChannelSender sender = channelSenders.stream()
                .filter(s -> s.getChannel() == request.getChannel())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No channel sender configured for: " + request.getChannel()));

        // 3. Dispatch through configured channel (Mock or Firebase)
        NotificationSendResult result = sender.send(request, deviceToken);

        // 4. Record Notification History
        NotificationStatus status = result.isSuccess() ? NotificationStatus.SENT : NotificationStatus.FAILED;
        LocalDateTime sentAt = result.isSuccess() ? LocalDateTime.now() : null;

        NotificationHistory history = NotificationHistory.builder()
                .recipientId(request.getRecipientId())
                .recipientType(request.getRecipientType())
                .channel(request.getChannel())
                .eventType(request.getEventType())
                .referenceId(request.getReferenceId())
                .title(request.getTitle())
                .body(request.getBody())
                .payload(payloadJson)
                .status(status)
                .errorMessage(result.getErrorMessage())
                .sentAt(sentAt)
                .build();

        NotificationHistory saved = notificationHistoryRepository.save(history);
        log.info("Dispatched notification [{}] to [{}] with status: {}", saved.getId(), request.getRecipientId(), status);
        return mapToDto(saved);
    }

    private String serializeData(Object data) {
        if (data == null) return null;
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to serialize notification data payload: {}", e.getMessage());
            return data.toString();
        }
    }

    public NotificationResponseDto mapToDto(NotificationHistory entity) {
        return NotificationResponseDto.builder()
                .id(entity.getId())
                .recipientId(entity.getRecipientId())
                .recipientType(entity.getRecipientType())
                .channel(entity.getChannel())
                .eventType(entity.getEventType())
                .referenceId(entity.getReferenceId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .payload(entity.getPayload())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .sentAt(entity.getSentAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
