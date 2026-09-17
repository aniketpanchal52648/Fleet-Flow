package com.example.matchingservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_offers", schema = "matching_service", indexes = {
        @Index(name = "idx_offer_shipment", columnList = "shipment_id"),
        @Index(name = "idx_offer_driver", columnList = "driver_id"),
        @Index(name = "idx_offer_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "offer_id", unique = true, nullable = false)
    private String offerId; // e.g. OFR-A1B2C3D4

    @Column(name = "shipment_id", nullable = false)
    private String shipmentId;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "vehicle_id")
    private String vehicleId;

    @Column(name = "offered_price", nullable = false)
    private Double offeredPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Version
    private Long version; // Optimistic locking for FCFS race conditions

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
