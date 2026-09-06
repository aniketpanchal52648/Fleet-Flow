package com.example.fleetpartner.entites;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "FLEET_PARTNER_VEHICLE")
@Data
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String vehicleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "fleet_partner_id",
            nullable = true
    )
    private FleetPartner fleetPartner;

    private String registrationNo;
    private String vehicleType;
    private double weightCapacity;
    private double volumeCapacity;
    private String dimension;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Availability availability;
}
