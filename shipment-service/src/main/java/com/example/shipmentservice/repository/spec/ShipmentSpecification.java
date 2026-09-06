package com.example.shipmentservice.repository.spec;

import com.example.shipmentservice.entities.Shipment;
import com.example.shipmentservice.entities.ShipmentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ShipmentSpecification {

    public static Specification<Shipment> filterShipments(
            ShipmentStatus status,
            String userId,
            String driverId,
            String vehicleId,
            String vehicleType
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("shipmentStatus"), status));
            }

            if (userId != null && !userId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId.trim()));
            }

            if (driverId != null && !driverId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("driverId"), driverId.trim()));
            }

            if (vehicleId != null && !vehicleId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("vehicleId"), vehicleId.trim()));
            }

            if (vehicleType != null && !vehicleType.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("vehicleType")),
                        vehicleType.trim().toLowerCase()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
