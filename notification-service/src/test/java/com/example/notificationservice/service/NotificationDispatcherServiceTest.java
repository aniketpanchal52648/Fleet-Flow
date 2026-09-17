package com.example.notificationservice.service;

import com.example.notificationservice.channel.NotificationChannelSender;
import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.dto.NotificationResponseDto;
import com.example.notificationservice.dto.NotificationSendResult;
import com.example.notificationservice.entities.DeviceToken;
import com.example.notificationservice.entities.NotificationChannel;
import com.example.notificationservice.entities.NotificationHistory;
import com.example.notificationservice.entities.NotificationStatus;
import com.example.notificationservice.entities.RecipientType;
import com.example.notificationservice.repository.DeviceTokenRepository;
import com.example.notificationservice.repository.NotificationHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherServiceTest {

    @Mock
    private NotificationChannelSender mockSender;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private NotificationHistoryRepository notificationHistoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private NotificationDispatcherService dispatcherService;

    @BeforeEach
    void setUp() {
        lenient().when(mockSender.getChannel()).thenReturn(NotificationChannel.PUSH);
        dispatcherService = new NotificationDispatcherService(
                List.of(mockSender),
                deviceTokenRepository,
                notificationHistoryRepository,
                objectMapper
        );
    }

    @Test
    @DisplayName("Should dispatch push notification successfully when active device token exists")
    void testDispatchSuccess() {
        NotificationRequest request = NotificationRequest.builder()
                .recipientId("driver-101")
                .recipientType(RecipientType.DRIVER)
                .channel(NotificationChannel.PUSH)
                .eventType("DELIVERY_OFFER_CREATED")
                .referenceId("offer-999")
                .title("New Offer")
                .body("You have an offer")
                .data(Map.of("offerId", "offer-999"))
                .build();

        DeviceToken deviceToken = DeviceToken.builder()
                .id("tok-1")
                .recipientId("driver-101")
                .recipientType(RecipientType.DRIVER)
                .deviceToken("fcm-test-token-xyz")
                .deviceType("ANDROID")
                .isActive(true)
                .build();

        when(deviceTokenRepository.findByRecipientIdAndRecipientTypeAndIsActiveTrue("driver-101", RecipientType.DRIVER))
                .thenReturn(Optional.of(deviceToken));

        when(mockSender.send(eq(request), eq(deviceToken)))
                .thenReturn(NotificationSendResult.builder().success(true).messageId("msg-123").build());

        when(notificationHistoryRepository.save(any(NotificationHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NotificationResponseDto response = dispatcherService.dispatch(request);

        assertNotNull(response);
        assertEquals(NotificationStatus.SENT, response.getStatus());
        assertEquals("driver-101", response.getRecipientId());
        assertNotNull(response.getSentAt());

        verify(mockSender, times(1)).send(eq(request), eq(deviceToken));
        verify(notificationHistoryRepository, times(1)).save(any(NotificationHistory.class));
    }

    @Test
    @DisplayName("Should record NO_DEVICE_TOKEN status when recipient has no registered active device")
    void testDispatchNoDeviceToken() {
        NotificationRequest request = NotificationRequest.builder()
                .recipientId("driver-unknown")
                .recipientType(RecipientType.DRIVER)
                .channel(NotificationChannel.PUSH)
                .eventType("DELIVERY_OFFER_CREATED")
                .referenceId("offer-888")
                .title("New Offer")
                .body("You have an offer")
                .build();

        when(deviceTokenRepository.findByRecipientIdAndRecipientTypeAndIsActiveTrue("driver-unknown", RecipientType.DRIVER))
                .thenReturn(Optional.empty());

        when(notificationHistoryRepository.save(any(NotificationHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NotificationResponseDto response = dispatcherService.dispatch(request);

        assertNotNull(response);
        assertEquals(NotificationStatus.NO_DEVICE_TOKEN, response.getStatus());
        assertTrue(response.getErrorMessage().contains("No active device token"));

        verify(mockSender, never()).send(any(), any());
        ArgumentCaptor<NotificationHistory> captor = ArgumentCaptor.forClass(NotificationHistory.class);
        verify(notificationHistoryRepository).save(captor.capture());
        assertEquals(NotificationStatus.NO_DEVICE_TOKEN, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should record FAILED status when push sender encounters an error")
    void testDispatchFailed() {
        NotificationRequest request = NotificationRequest.builder()
                .recipientId("driver-101")
                .recipientType(RecipientType.DRIVER)
                .channel(NotificationChannel.PUSH)
                .eventType("DELIVERY_OFFER_CREATED")
                .referenceId("offer-777")
                .title("New Offer")
                .body("You have an offer")
                .build();

        DeviceToken deviceToken = DeviceToken.builder()
                .id("tok-1")
                .recipientId("driver-101")
                .recipientType(RecipientType.DRIVER)
                .deviceToken("fcm-invalid-token")
                .deviceType("IOS")
                .isActive(true)
                .build();

        when(deviceTokenRepository.findByRecipientIdAndRecipientTypeAndIsActiveTrue("driver-101", RecipientType.DRIVER))
                .thenReturn(Optional.of(deviceToken));

        when(mockSender.send(eq(request), eq(deviceToken)))
                .thenReturn(NotificationSendResult.builder().success(false).errorMessage("FCM invalid registration token").build());

        when(notificationHistoryRepository.save(any(NotificationHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NotificationResponseDto response = dispatcherService.dispatch(request);

        assertNotNull(response);
        assertEquals(NotificationStatus.FAILED, response.getStatus());
        assertEquals("FCM invalid registration token", response.getErrorMessage());
        assertNull(response.getSentAt());
    }
}
