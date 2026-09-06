package com.example.userservice.dto;

import com.example.userservice.entities.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
