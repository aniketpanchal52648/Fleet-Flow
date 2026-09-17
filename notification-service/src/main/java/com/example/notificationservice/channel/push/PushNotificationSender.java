package com.example.notificationservice.channel.push;

import com.example.notificationservice.dto.NotificationSendResult;

import java.util.Map;

public interface PushNotificationSender {

    NotificationSendResult sendPush(String deviceToken, String title, String body, Map<String, String> data);

}
