package com.example.matchingservice.scheduler;

import com.example.matchingservice.entities.DeliveryOffer;
import com.example.matchingservice.entities.OfferStatus;
import com.example.matchingservice.repository.DeliveryOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OfferExpirationScheduler {

    private final DeliveryOfferRepository offerRepository;

    @Scheduled(fixedDelay = 30000) // Runs every 30 seconds
    @Transactional
    public void expireStaleOffers() {
        LocalDateTime now = LocalDateTime.now();
        List<DeliveryOffer> expiredOffers = offerRepository.findAllByStatusAndExpiresAtBefore(OfferStatus.OPEN, now);

        if (!expiredOffers.isEmpty()) {
            for (DeliveryOffer offer : expiredOffers) {
                offer.setStatus(OfferStatus.EXPIRED);
            }
            offerRepository.saveAll(expiredOffers);
            log.info("OfferExpirationScheduler: Auto-expired {} delivery offer(s) whose 5-minute countdown elapsed.",
                    expiredOffers.size());
        }
    }
}
