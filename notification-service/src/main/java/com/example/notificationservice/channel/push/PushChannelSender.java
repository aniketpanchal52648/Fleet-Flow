package com.example.notificationservice.channel.push;

import com.example.notificationservice.channel.NotificationChannelSender;
import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.dto.NotificationSendResult;
import com.example.notificationservice.entities.DeviceToken;
import com.example.notificationservice.entities.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushChannelSender implements NotificationChannelSender {

    private final PushNotificationSender pushNotificationSender;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public NotificationSendResult send(NotificationRequest request, DeviceToken deviceToken) {
        return pushNotificationSender.sendPush(
                deviceToken.getDeviceToken(),
                request.getTitle(),
                request.getBody(),
                request.getData()
        );
    }
}
