package com.example.fleetpartner.service;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.VehicleDto;
import com.example.fleetpartner.entites.Status;

import java.util.List;

public interface VehicleService {

    VehicleDto createVehicle(VehicleDto vehicleDto);

    List<VehicleDto> getVehicles(Status status, String fleetPartnerId, String vehicleType, Double capacity, String dimension);

    VehicleDto getVehicleByVehicleId(String vehicleId);

    VehicleDto updateVehicle(String vehicleId, VehicleDto vehicleDto);

    ApiResponseDto deleteVehicle(String vehicleId);

    ApiResponseDto activateVehicle(String vehicleId);

    ApiResponseDto inactivateVehicle(String vehicleId);
}
