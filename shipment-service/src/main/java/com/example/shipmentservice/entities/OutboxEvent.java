package com.example.shipmentservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events", schema = "shipment_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String aggregateType; // "SHIPMENT"

    @Column(nullable = false)
    private String aggregateId;   // shipmentId (SHP-XXXX)

    @Column(nullable = false)
    private String eventType;     // "SHIPMENT_CREATED"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;       // JSON serialized event

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;  // PENDING, SENT, FAILED

    private int retryCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
}