package com.example.shipmentservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_shipment", schema = "shipment_service")
@Data
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "SHIPMENT_ID", unique = true)
    private String shipmentId;

    @Column(name = "USER_ID")
    private String userId;

    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "PICKUP_LOCATION_ID")
    private UserAddress pickupLocation;

    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "DROP_LOCATION_ID")
    private UserAddress dropLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "SHIPMENT_STATUS")
    private ShipmentStatus shipmentStatus;

    private String driverId;
    private String vehicleId;
    private String vehicleType;
    private Double weightKg;
    private String cancellationReason;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
