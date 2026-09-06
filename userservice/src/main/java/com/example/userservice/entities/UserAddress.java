package com.example.userservice.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "USER_ADDRESS")
@Data
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private Integer pincode;

    private String state;

    private String district;
    private String addressLineOne;
    private String addressLineTwo;

    private AddressType addressType;

    private String geoLocation;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

}
