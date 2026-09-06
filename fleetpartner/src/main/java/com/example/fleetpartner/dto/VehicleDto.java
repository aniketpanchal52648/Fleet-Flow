package com.example.fleetpartner.dto;

import com.example.fleetpartner.entites.Availability;
import com.example.fleetpartner.entites.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDto {

    private String id;
    private String vehicleId;
    private String fleetPartnerId;
    private String registrationNo;
    private String vehicleType;
    private Double weightCapacity;
    private Double volumeCapacity;
    private String dimension;
    private Status status;
    private Availability availability;
}
