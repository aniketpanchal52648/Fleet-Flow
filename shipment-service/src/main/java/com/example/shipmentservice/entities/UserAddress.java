package com.example.shipmentservice.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "shipment_user_address", schema = "shipment_service")
@Data
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private Integer pincode;

    private String state;
    private String userId;

    private String district;
    private String addressLineOne;
    private String addressLineTwo;

    private AddressType addressType;

    private String geoLocation;


}
