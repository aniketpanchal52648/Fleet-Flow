package com.example.notificationservice.channel;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.dto.NotificationSendResult;
import com.example.notificationservice.entities.DeviceToken;
import com.example.notificationservice.entities.NotificationChannel;

public interface NotificationChannelSender {

    NotificationChannel getChannel();

    NotificationSendResult send(NotificationRequest request, DeviceToken deviceToken);

}
