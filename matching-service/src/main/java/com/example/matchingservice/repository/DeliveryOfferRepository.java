package com.example.matchingservice.repository;

import com.example.matchingservice.entities.DeliveryOffer;
import com.example.matchingservice.entities.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryOfferRepository extends JpaRepository<DeliveryOffer, String> {

    Optional<DeliveryOffer> findByOfferId(String offerId);

    List<DeliveryOffer> findAllByShipmentId(String shipmentId);

    List<DeliveryOffer> findAllByShipmentIdAndStatus(String shipmentId, OfferStatus status);

    List<DeliveryOffer> findAllByDriverIdAndStatus(String driverId, OfferStatus status);

    List<DeliveryOffer> findAllByStatusAndExpiresAtBefore(OfferStatus status, LocalDateTime threshold);

    boolean existsByShipmentIdAndStatus(String shipmentId, OfferStatus status);
}
