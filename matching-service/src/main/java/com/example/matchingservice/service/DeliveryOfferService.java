package com.example.matchingservice.service;

import com.example.matchingservice.dto.DeliveryOfferDto;
import com.example.matchingservice.dto.OfferAcceptResponseDto;
import com.example.matchingservice.entities.DeliveryOffer;
import com.example.matchingservice.entities.OfferStatus;
import com.example.matchingservice.entities.OutboxEvent;
import com.example.matchingservice.entities.OutboxStatus;
import com.example.matchingservice.repository.DeliveryOfferRepository;
import com.example.matchingservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorDetail;
import org.example.error.ErrorException;
import org.example.event.DeliveryOfferAcceptedEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryOfferService {

    private final DeliveryOfferRepository offerRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public OfferAcceptResponseDto acceptOffer(String offerId) {
        // 1. Fetch offer or throw 404
        DeliveryOffer offer = offerRepository.findByOfferId(offerId)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("OFFER_NOT_FOUND", "offerId", "Offer not found: " + offerId)));

        LocalDateTime now = LocalDateTime.now();

        // 2. Validate offer is OPEN and not expired
        if (offer.getStatus() != OfferStatus.OPEN || now.isAfter(offer.getExpiresAt())) {
            throw new ErrorException(new ErrorDetail("OFFER_EXPIRED", "offerId",
                    "This offer is no longer open or has expired. Current status: " + offer.getStatus()));
        }

        // 3. Fast pre-check: Check if another driver already accepted this shipment
        boolean alreadyAccepted = offerRepository.existsByShipmentIdAndStatus(offer.getShipmentId(), OfferStatus.ACCEPTED);
        if (alreadyAccepted) {
            log.warn("Driver [{}] tried to accept offer [{}] but shipment [{}] was already won by another driver.",
                    offer.getDriverId(), offerId, offer.getShipmentId());
            throw new ErrorException(new ErrorDetail("OFFER_ALREADY_ACCEPTED", "offerId",
                    "This delivery job has already been accepted by another driver (First-Come, First-Served)."));
        }

        try {
            // 4. Mark clicked offer ACCEPTED and flush to database
            // The PostgreSQL partial unique index 'uq_shipment_accepted_offer' serializes concurrent transactions.
            // If two drivers click at the exact same millisecond, the database engine commits the first and throws
            // DataIntegrityViolationException on the second!
            offer.setStatus(OfferStatus.ACCEPTED);
            DeliveryOffer acceptedOffer = offerRepository.saveAndFlush(offer);

            // 5. Transition all other OPEN sibling offers for this shipment to EXPIRED
            List<DeliveryOffer> siblingOffers = offerRepository.findAllByShipmentIdAndStatus(offer.getShipmentId(), OfferStatus.OPEN);
            for (DeliveryOffer sibling : siblingOffers) {
                sibling.setStatus(OfferStatus.EXPIRED);
            }
            offerRepository.saveAll(siblingOffers);

            log.info("Driver [{}] successfully won offer [{}] for shipment [{}]. Expired {} competing offer(s).",
                    acceptedOffer.getDriverId(), offerId, acceptedOffer.getShipmentId(), siblingOffers.size());

            // 6. Save Transactional Outbox Event (for Dispatch Service & Notifications)
            saveOfferAcceptedOutboxEvent(acceptedOffer);

            return OfferAcceptResponseDto.builder()
                    .offerId(acceptedOffer.getOfferId())
                    .shipmentId(acceptedOffer.getShipmentId())
                    .driverId(acceptedOffer.getDriverId())
                    .vehicleId(acceptedOffer.getVehicleId())
                    .agreedPrice(acceptedOffer.getOfferedPrice())
                    .status(acceptedOffer.getStatus())
                    .acceptedAt(now)
                    .message("Congratulations! You have been assigned to fulfill this shipment.")
                    .build();

        } catch (DataIntegrityViolationException ex) {
            log.warn("FCFS Race Condition Arbitrated: Concurrent collision for shipment [{}]. Offer [{}] rejected.",
                    offer.getShipmentId(), offerId);
            throw new ErrorException(new ErrorDetail("OFFER_ALREADY_ACCEPTED", "offerId",
                    "This delivery job has already been accepted by another driver (First-Come, First-Served)."));
        }
    }

    @Transactional
    public DeliveryOfferDto rejectOffer(String offerId) {
        DeliveryOffer offer = offerRepository.findByOfferId(offerId)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("OFFER_NOT_FOUND", "offerId", "Offer not found: " + offerId)));

        if (offer.getStatus() == OfferStatus.OPEN) {
            offer.setStatus(OfferStatus.REJECTED);
            DeliveryOffer saved = offerRepository.save(offer);
            log.info("Driver [{}] declined offer [{}]", saved.getDriverId(), offerId);
            return mapToDto(saved);
        }

        return mapToDto(offer);
    }

    @Transactional(readOnly = true)
    public DeliveryOfferDto getOffer(String offerId) {
        DeliveryOffer offer = offerRepository.findByOfferId(offerId)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("OFFER_NOT_FOUND", "offerId", "Offer not found: " + offerId)));
        return mapToDto(offer);
    }

    @Transactional(readOnly = true)
    public List<DeliveryOfferDto> getDriverOffers(String driverId, OfferStatus status) {
        List<DeliveryOffer> offers;
        if (status != null) {
            offers = offerRepository.findAllByDriverIdAndStatus(driverId, status);
        } else {
            offers = offerRepository.findAllByDriverIdAndStatus(driverId, OfferStatus.OPEN);
        }
        return offers.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private void saveOfferAcceptedOutboxEvent(DeliveryOffer offer) {
        try {
            DeliveryOfferAcceptedEvent event = DeliveryOfferAcceptedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("DELIVERY_OFFER_ACCEPTED")
                    .offerId(offer.getOfferId())
                    .shipmentId(offer.getShipmentId())
                    .driverId(offer.getDriverId())
                    .vehicleId(offer.getVehicleId())
                    .agreedPrice(offer.getOfferedPrice())
                    .acceptedAt(LocalDateTime.now())
                    .build();

            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateType("DELIVERY_OFFER")
                    .aggregateId(offer.getOfferId())
                    .eventType("DELIVERY_OFFER_ACCEPTED")
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(outbox);
        } catch (Exception e) {
            log.error("Failed to serialize DeliveryOfferAcceptedEvent for offer: {}", offer.getOfferId(), e);
        }
    }

    private DeliveryOfferDto mapToDto(DeliveryOffer entity) {
        return DeliveryOfferDto.builder()
                .offerId(entity.getOfferId())
                .shipmentId(entity.getShipmentId())
                .driverId(entity.getDriverId())
                .vehicleId(entity.getVehicleId())
                .offeredPrice(entity.getOfferedPrice())
                .status(entity.getStatus())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
