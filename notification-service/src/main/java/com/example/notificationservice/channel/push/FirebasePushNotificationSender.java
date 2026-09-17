package com.example.notificationservice.channel.push;

import com.example.notificationservice.dto.NotificationSendResult;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.notification.provider", havingValue = "firebase")
@Slf4j
public class FirebasePushNotificationSender implements PushNotificationSender {

    private final ResourceLoader resourceLoader;

    @Value("${app.firebase.credentials-path:classpath:firebase-service-account.json}")
    private String credentialsPath;

    public FirebasePushNotificationSender(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        log.info("Initialized FirebasePushNotificationSender (FIREBASE mode active).");
    }

    @PostConstruct
    public void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                Resource resource = resourceLoader.getResource(credentialsPath);
                if (resource.exists()) {
                    try (InputStream serviceAccount = resource.getInputStream()) {
                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .build();
                        FirebaseApp.initializeApp(options);
                        log.info("Successfully initialized FirebaseApp using credentials from: {}", credentialsPath);
                    }
                } else {
                    log.warn("Firebase credentials not found at [{}]. Push notifications will fail until credentials are provided.", credentialsPath);
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize FirebaseApp: {}", e.getMessage(), e);
        }
    }

    @Override
    public NotificationSendResult sendPush(String deviceToken, String title, String body, Map<String, String> data) {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                return NotificationSendResult.failure("FirebaseApp is not initialized (credentials missing)");
            }

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder builder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification);

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            Message message = builder.build();
            String messageId = FirebaseMessaging.getInstance().send(message);

            log.info("Successfully sent real Firebase push notification to device [{}]. Message ID: {}", deviceToken, messageId);
            return NotificationSendResult.ok(messageId);

        } catch (Exception e) {
            log.error("Failed to dispatch Firebase push notification to device [{}]: {}", deviceToken, e.getMessage());
            return NotificationSendResult.failure(e.getMessage());
        }
    }
}
