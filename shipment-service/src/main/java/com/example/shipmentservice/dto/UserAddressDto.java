package com.example.shipmentservice.dto;

import com.example.shipmentservice.entities.AddressType;
import lombok.Data;

@Data
public class UserAddressDto {


    private String id;

    private Integer pincode;

    private String state;

    private String district;
    private String addressLineOne;
    private String addressLineTwo;

    private AddressType addressType;

    private String geoLocation;
    private String userId;

}
