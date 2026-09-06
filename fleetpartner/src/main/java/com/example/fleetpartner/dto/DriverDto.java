package com.example.fleetpartner.dto;

import com.example.fleetpartner.entites.Availability;
import com.example.fleetpartner.entites.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDto {

    private String id;
    private String driverId;
    private String partnerId;
    private String keycloakId;
    private String name;
    private String phoneNo;
    private String licencesNo;
    private Date licencesExpiry;
    private Status status;
    private Availability availability;
}
