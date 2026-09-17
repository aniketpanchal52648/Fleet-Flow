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
public class DeliveryOfferAcceptedEvent {
    private String eventId;
    private String eventType; // "DELIVERY_OFFER_ACCEPTED"
    private String offerId;
    private String shipmentId;
    private String driverId;
    private String vehicleId;
    private Double agreedPrice;
    private LocalDateTime acceptedAt;
}
