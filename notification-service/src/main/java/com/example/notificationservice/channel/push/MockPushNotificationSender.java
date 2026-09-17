package com.example.notificationservice.channel.push;

import com.example.notificationservice.dto.NotificationSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.notification.provider", havingValue = "dev", matchIfMissing = true)
@Slf4j
public class MockPushNotificationSender implements PushNotificationSender {

    public MockPushNotificationSender() {
        log.info("Initialized MockPushNotificationSender (DEV mode active). Notifications will be logged to console and saved to database.");
    }

    @Override
    public NotificationSendResult sendPush(String deviceToken, String title, String body, Map<String, String> data) {
        String mockMessageId = "mock-fcm-" + UUID.randomUUID().toString();

        log.info("\n" +
                "╔════════════════════════════════════════════════════════════════════════════╗\n" +
                "║ 🔔 [MOCK PUSH NOTIFICATION DISPATCHED]                                      ║\n" +
                "╠════════════════════════════════════════════════════════════════════════════╣\n" +
                "║ Device Token : {}\n" +
                "║ Title        : {}\n" +
                "║ Body         : {}\n" +
                "║ Data Payload : {}\n" +
                "║ Mock Msg ID  : {}\n" +
                "╚════════════════════════════════════════════════════════════════════════════╝",
                deviceToken, title, body, data, mockMessageId);

        return NotificationSendResult.ok(mockMessageId);
    }
}
