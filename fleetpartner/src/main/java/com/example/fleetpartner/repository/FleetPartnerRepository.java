package com.example.fleetpartner.repository;

import com.example.fleetpartner.entites.FleetPartner;
import com.example.fleetpartner.entites.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FleetPartnerRepository extends JpaRepository<FleetPartner, String> {

    Optional<FleetPartner> findByPartnerId(String partnerId);

    boolean existsByPartnerId(String partnerId);

    List<FleetPartner> findByStatus(Status status);

    List<FleetPartner> findByStatusNot(Status status);
}
