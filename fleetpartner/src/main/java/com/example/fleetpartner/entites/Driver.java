package com.example.fleetpartner.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "FLEET_DRIVER")
@Data
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String driverId;

    private String partnerId;
    private String keycloakId;
    private String name;
    private String phoneNo;
    private String licencesNo;
    private Date licencesExpiry;
    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Availability availability;
}
