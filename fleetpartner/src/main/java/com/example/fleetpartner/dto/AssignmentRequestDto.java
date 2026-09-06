package com.example.fleetpartner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentRequestDto {

    private String vehicleId;
    private String driverId;
}
