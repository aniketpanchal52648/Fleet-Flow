package com.example.fleetpartner.repository.spec;

import com.example.fleetpartner.entites.FleetPartner;
import com.example.fleetpartner.entites.Status;
import com.example.fleetpartner.entites.Vehicle;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class VehicleSpecification {

    public static Specification<Vehicle> filterVehicles(
            Status status,
            String fleetPartnerId,
            String vehicleType,
            Double minCapacity,
            String dimension
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Status effectiveStatus = (status != null) ? status : Status.ACTIVE;
            predicates.add(criteriaBuilder.equal(root.get("status"), effectiveStatus));

            if (fleetPartnerId != null && !fleetPartnerId.trim().isEmpty()) {
                Join<Vehicle, FleetPartner> partnerJoin = root.join("fleetPartner", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(partnerJoin.get("partnerId"), fleetPartnerId.trim()));
            }

            if (vehicleType != null && !vehicleType.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("vehicleType")),
                        vehicleType.trim().toLowerCase()
                ));
            }

            if (minCapacity != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("weightCapacity"), minCapacity));
            }

            if (dimension != null && !dimension.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("dimension")),
                        "%" + dimension.trim().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
