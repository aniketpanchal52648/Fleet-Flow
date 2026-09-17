package com.example.trackingservice.controller;

import com.example.trackingservice.dto.ShipmentLiveTrackingResponseDto;
import com.example.trackingservice.service.ShipmentTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web/v1/tracking-service/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShipmentTrackingController {

    private final ShipmentTrackingService shipmentTrackingService;

    @GetMapping("/{shipmentId}/live-location")
    public ResponseEntity<ShipmentLiveTrackingResponseDto> getShipmentLiveLocation(@PathVariable String shipmentId) {
        log.info("REST: Querying live tracking location for shipment [{}]", shipmentId);
        ShipmentLiveTrackingResponseDto response = shipmentTrackingService.getShipmentLiveLocation(shipmentId);
        return ResponseEntity.ok(response);
    }
}
