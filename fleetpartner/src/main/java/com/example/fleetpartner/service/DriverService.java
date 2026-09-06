package com.example.fleetpartner.service;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.DriverDto;
import com.example.fleetpartner.entites.Availability;
import com.example.fleetpartner.entites.Status;

import java.util.List;

public interface DriverService {

    DriverDto createDriver(DriverDto driverDto);

    List<DriverDto> getDrivers(Status status, String partnerId, Availability availability);

    DriverDto getDriverByDriverId(String driverId);

    DriverDto updateDriver(String driverId, DriverDto driverDto);

    ApiResponseDto activateDriver(String driverId);

    ApiResponseDto inactivateDriver(String driverId);

    ApiResponseDto deleteDriver(String driverId);
}
