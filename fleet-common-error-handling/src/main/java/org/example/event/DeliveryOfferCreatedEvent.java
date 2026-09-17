package org.example.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryOfferCreatedEvent {
    private String eventId;
    private String eventType; // "DELIVERY_OFFER_CREATED"
    private String offerId;
    private String shipmentId;
    private String driverId;
    private String vehicleId;
    private Double offeredPrice;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
