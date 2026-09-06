package com.example.fleetpartner.entites;

import com.example.fleetpartner.util.StringListConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "FLEET_PARTNER_DLS")
@Data
public class FleetPartner {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;


    @Column(name = "PARTNER_ID",unique = true,nullable = false)
    private String partnerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "PARTNER_TYPE",nullable = false)
    private PartnerType partnerType;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "OWNER_NAME",nullable = false)
    private String ownerName;

    @Column(name = "KEYCLOAK_ID")
    private String keycloakId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS",nullable = false)
    private Status status=Status.PENDING;

    @Lob // 👈 Tells the DB to use a CLOB/TEXT type
    @Convert(converter = StringListConverter.class) // 👈 Automatically converts List <-> JSON String
    @Column(name = "SERVICE_AREAS", columnDefinition = "CLOB") // Use "TEXT" or "LONGTEXT" depending on your SQL dialect
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "fleetPartner")
    private List<Vehicle> vehicles = new ArrayList<>();




}
