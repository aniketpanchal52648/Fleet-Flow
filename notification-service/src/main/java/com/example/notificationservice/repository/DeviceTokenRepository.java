package com.example.notificationservice.repository;

import com.example.notificationservice.entities.DeviceToken;
import com.example.notificationservice.entities.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {

    Optional<DeviceToken> findByRecipientIdAndRecipientTypeAndIsActiveTrue(String recipientId, RecipientType recipientType);

    Optional<DeviceToken> findByDeviceToken(String deviceToken);

    List<DeviceToken> findAllByRecipientIdAndRecipientType(String recipientId, RecipientType recipientType);
}
