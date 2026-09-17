package com.example.matchingservice.service;

import com.example.matchingservice.dto.DeliveryOfferDto;
import com.example.matchingservice.dto.OfferAcceptResponseDto;
import com.example.matchingservice.entities.DeliveryOffer;
import com.example.matchingservice.entities.OfferStatus;
import com.example.matchingservice.entities.OutboxEvent;
import com.example.matchingservice.repository.DeliveryOfferRepository;
import com.example.matchingservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.error.ErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryOfferServiceTest {

    @Mock
    private DeliveryOfferRepository offerRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DeliveryOfferService offerService;

    private DeliveryOffer testOffer;

    @BeforeEach
    void setUp() {
        testOffer = DeliveryOffer.builder()
                .offerId("OFR-12345678")
                .shipmentId("SHP-ABCDEF01")
                .driverId("DRV-101")
                .vehicleId("VEH-101")
                .offeredPrice(150.0)
                .status(OfferStatus.OPEN)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    @Test
    void testAcceptOffer_Success() throws Exception {
        DeliveryOffer siblingOffer = DeliveryOffer.builder()
                .offerId("OFR-87654321")
                .shipmentId("SHP-ABCDEF01")
                .driverId("DRV-102")
                .status(OfferStatus.OPEN)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(offerRepository.findByOfferId("OFR-12345678")).thenReturn(Optional.of(testOffer));
        when(offerRepository.existsByShipmentIdAndStatus("SHP-ABCDEF01", OfferStatus.ACCEPTED)).thenReturn(false);
        when(offerRepository.saveAndFlush(testOffer)).thenReturn(testOffer);
        when(offerRepository.findAllByShipmentIdAndStatus("SHP-ABCDEF01", OfferStatus.OPEN))
                .thenReturn(List.of(siblingOffer));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventType\":\"DELIVERY_OFFER_ACCEPTED\"}");

        OfferAcceptResponseDto response = offerService.acceptOffer("OFR-12345678");

        assertNotNull(response);
        assertEquals("OFR-12345678", response.getOfferId());
        assertEquals("DRV-101", response.getDriverId());
        assertEquals(OfferStatus.ACCEPTED, testOffer.getStatus());
        assertEquals(OfferStatus.EXPIRED, siblingOffer.getStatus());

        verify(offerRepository).saveAndFlush(testOffer);
        verify(offerRepository).saveAll(List.of(siblingOffer));
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void testAcceptOffer_AlreadyAcceptedByAnotherDriver_PreCheck_ThrowsException() {
        when(offerRepository.findByOfferId("OFR-12345678")).thenReturn(Optional.of(testOffer));
        when(offerRepository.existsByShipmentIdAndStatus("SHP-ABCDEF01", OfferStatus.ACCEPTED)).thenReturn(true);

        ErrorException ex = assertThrows(ErrorException.class, () -> offerService.acceptOffer("OFR-12345678"));
        assertEquals("OFFER_ALREADY_ACCEPTED", ex.getErrorDetail().getCode());

        verify(offerRepository, never()).saveAndFlush(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void testAcceptOffer_ConcurrentCollision_DatabaseUniqueIndexViolation_ThrowsException() {
        when(offerRepository.findByOfferId("OFR-12345678")).thenReturn(Optional.of(testOffer));
        when(offerRepository.existsByShipmentIdAndStatus("SHP-ABCDEF01", OfferStatus.ACCEPTED)).thenReturn(false);
        when(offerRepository.saveAndFlush(testOffer))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint uq_shipment_accepted_offer"));

        ErrorException ex = assertThrows(ErrorException.class, () -> offerService.acceptOffer("OFR-12345678"));
        assertEquals("OFFER_ALREADY_ACCEPTED", ex.getErrorDetail().getCode());

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void testAcceptOffer_Expired_ThrowsException() {
        testOffer.setStatus(OfferStatus.EXPIRED);
        when(offerRepository.findByOfferId("OFR-12345678")).thenReturn(Optional.of(testOffer));

        ErrorException ex = assertThrows(ErrorException.class, () -> offerService.acceptOffer("OFR-12345678"));
        assertEquals("OFFER_EXPIRED", ex.getErrorDetail().getCode());
    }

    @Test
    void testRejectOffer_Success() {
        when(offerRepository.findByOfferId("OFR-12345678")).thenReturn(Optional.of(testOffer));
        when(offerRepository.save(testOffer)).thenReturn(testOffer);

        DeliveryOfferDto dto = offerService.rejectOffer("OFR-12345678");

        assertNotNull(dto);
        assertEquals(OfferStatus.REJECTED, testOffer.getStatus());
        verify(offerRepository).save(testOffer);
    }
}
