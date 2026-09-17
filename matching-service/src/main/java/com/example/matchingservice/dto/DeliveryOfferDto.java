package com.example.matchingservice.dto;

import com.example.matchingservice.entities.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryOfferDto {
    private String offerId;
    private String shipmentId;
    private String driverId;
    private String vehicleId;
    private Double offeredPrice;
    private OfferStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
