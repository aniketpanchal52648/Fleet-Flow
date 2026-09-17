package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationResponseDto;
import com.example.notificationservice.entities.NotificationHistory;
import com.example.notificationservice.entities.RecipientType;
import com.example.notificationservice.repository.NotificationHistoryRepository;
import com.example.notificationservice.service.NotificationDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/web/v1/notification-service/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationHistoryController {

    private final NotificationHistoryRepository notificationHistoryRepository;
    private final NotificationDispatcherService notificationDispatcherService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
            @RequestParam String recipientId,
            @RequestParam(required = false) RecipientType recipientType) {
        log.info("REST: Querying notification history for recipient [{}] ({})", recipientId, recipientType);

        List<NotificationHistory> historyList;
        if (recipientType != null) {
            historyList = notificationHistoryRepository.findAllByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(
                    recipientId, recipientType);
        } else {
            historyList = notificationHistoryRepository.findAllByRecipientIdOrderByCreatedAtDesc(recipientId);
        }

        List<NotificationResponseDto> responseList = historyList.stream()
                .map(notificationDispatcherService::mapToDto)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDto> getNotificationById(@PathVariable String id) {
        log.info("REST: Fetching notification history record [{}]", id);
        return notificationHistoryRepository.findById(id)
                .map(notificationDispatcherService::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
