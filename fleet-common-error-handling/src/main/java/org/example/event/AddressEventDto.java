package org.example.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEventDto {
    private String addressLineOne;
    private String addressLineTwo;
    private String district;
    private String state;
    private Integer pincode;
    private String geoLocation;
    private String addressType;
}