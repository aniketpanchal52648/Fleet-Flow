package com.example.fleetpartner.controller;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.FleetPartnerDto;
import com.example.fleetpartner.entites.Status;
import com.example.fleetpartner.service.FleetPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("web/v1/fleet-partner-service/partner")
@RequiredArgsConstructor
public class FleetPartnerController {

    private final FleetPartnerService fleetPartnerService;

    @PostMapping
    public ResponseEntity<FleetPartnerDto> createPartner(@RequestBody FleetPartnerDto partnerDto) {
        FleetPartnerDto created = fleetPartnerService.createPartner(partnerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<FleetPartnerDto>> getAllPartners(@RequestParam(required = false) Status status) {
        return ResponseEntity.ok(fleetPartnerService.getAllPartners(status));
    }

    @GetMapping("/{partnerId}")
    public ResponseEntity<FleetPartnerDto> getPartnerById(@PathVariable String partnerId) {
        return ResponseEntity.ok(fleetPartnerService.getPartnerByPartnerId(partnerId));
    }

    @PutMapping("/{partnerId}")
    public ResponseEntity<FleetPartnerDto> updatePartner(
            @PathVariable String partnerId,
            @RequestBody FleetPartnerDto partnerDto) {
        return ResponseEntity.ok(fleetPartnerService.updatePartner(partnerId, partnerDto));
    }

    @PatchMapping("/{partnerId}/delete")
    public ResponseEntity<ApiResponseDto> deletePartner(@PathVariable String partnerId) {
        return ResponseEntity.ok(fleetPartnerService.deletePartner(partnerId));
    }

    @PatchMapping("/{partnerId}/activate")
    public ResponseEntity<ApiResponseDto> activatePartner(@PathVariable String partnerId) {
        return ResponseEntity.ok(fleetPartnerService.activatePartner(partnerId));
    }

    @PatchMapping("/{partnerId}/inactivate")
    public ResponseEntity<ApiResponseDto> inactivatePartner(@PathVariable String partnerId) {
        return ResponseEntity.ok(fleetPartnerService.inactivatePartner(partnerId));
    }

    @PostMapping("/{partnerId}/vehicles")
    public ResponseEntity<ApiResponseDto> associateVehicles(
            @PathVariable String partnerId,
            @RequestBody List<String> vehicleIds) {
        return ResponseEntity.ok(fleetPartnerService.associateVehicles(partnerId, vehicleIds));
    }

    @DeleteMapping("/{partnerId}/vehicles")
    public ResponseEntity<ApiResponseDto> disassociateVehicles(
            @PathVariable String partnerId,
            @RequestBody List<String> vehicleIds) {
        return ResponseEntity.ok(fleetPartnerService.disassociateVehicles(partnerId, vehicleIds));
    }
}
