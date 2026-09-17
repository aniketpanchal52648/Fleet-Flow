package com.example.notificationservice.repository;

import com.example.notificationservice.entities.NotificationHistory;
import com.example.notificationservice.entities.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, String> {

    List<NotificationHistory> findAllByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(String recipientId, RecipientType recipientType);

    List<NotificationHistory> findAllByRecipientIdOrderByCreatedAtDesc(String recipientId);
}
