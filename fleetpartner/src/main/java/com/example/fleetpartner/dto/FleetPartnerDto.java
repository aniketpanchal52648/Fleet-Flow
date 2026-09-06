package com.example.fleetpartner.dto;

import com.example.fleetpartner.entites.PartnerType;
import com.example.fleetpartner.entites.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetPartnerDto {

    private String id;
    private String partnerId;
    private PartnerType partnerType;
    private String name;
    private String ownerName;
    private String keycloakId;
    private Status status;
    private List<String> tags = new ArrayList<>();
    private List<VehicleDto> vehicles = new ArrayList<>();
}
