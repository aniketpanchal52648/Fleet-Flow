package com.example.shipmentservice.repository;

import com.example.shipmentservice.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String>, JpaSpecificationExecutor<Shipment> {

    Optional<Shipment> findByShipmentId(String shipmentId);

    boolean existsByShipmentId(String shipmentId);

    List<Shipment> findByUserId(String userId);
}
