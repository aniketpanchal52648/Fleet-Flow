package com.example.matchingservice.controller;

import com.example.matchingservice.dto.DeliveryOfferDto;
import com.example.matchingservice.dto.OfferAcceptResponseDto;
import com.example.matchingservice.entities.OfferStatus;
import com.example.matchingservice.service.DeliveryOfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/web/v1/matching-service/delivery-offers")
@RequiredArgsConstructor
@Slf4j
public class DeliveryOfferController {

    private final DeliveryOfferService deliveryOfferService;

    @PostMapping("/{offerId}/accept")
    public ResponseEntity<OfferAcceptResponseDto> acceptOffer(@PathVariable String offerId) {
        log.info("REST: Received request to accept offer [{}]", offerId);
        OfferAcceptResponseDto response = deliveryOfferService.acceptOffer(offerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{offerId}/reject")
    public ResponseEntity<DeliveryOfferDto> rejectOffer(@PathVariable String offerId) {
        log.info("REST: Received request to reject offer [{}]", offerId);
        DeliveryOfferDto response = deliveryOfferService.rejectOffer(offerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<DeliveryOfferDto> getOffer(@PathVariable String offerId) {
        log.info("REST: Received request to fetch offer [{}]", offerId);
        DeliveryOfferDto response = deliveryOfferService.getOffer(offerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryOfferDto>> getDriverOffers(
            @RequestParam String driverId,
            @RequestParam(required = false) OfferStatus status) {
        log.info("REST: Received request to list offers for driver [{}] with status [{}]", driverId, status);
        List<DeliveryOfferDto> offers = deliveryOfferService.getDriverOffers(driverId, status);
        return ResponseEntity.ok(offers);
    }
}
