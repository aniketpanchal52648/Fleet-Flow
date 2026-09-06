package com.example.fleetpartner.service;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.FleetPartnerDto;
import com.example.fleetpartner.entites.Status;

import java.util.List;

public interface FleetPartnerService {

    FleetPartnerDto createPartner(FleetPartnerDto partnerDto);

    List<FleetPartnerDto> getAllPartners(Status status);

    FleetPartnerDto getPartnerByPartnerId(String partnerId);

    FleetPartnerDto updatePartner(String partnerId, FleetPartnerDto partnerDto);

    ApiResponseDto activatePartner(String partnerId);

    ApiResponseDto inactivatePartner(String partnerId);

    ApiResponseDto deletePartner(String partnerId);

    ApiResponseDto associateVehicles(String partnerId, List<String> vehicleIds);

    ApiResponseDto disassociateVehicles(String partnerId, List<String> vehicleIds);
}
