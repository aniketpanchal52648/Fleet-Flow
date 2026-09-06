package com.example.fleetpartner.repository.spec;

import com.example.fleetpartner.entites.Availability;
import com.example.fleetpartner.entites.Driver;
import com.example.fleetpartner.entites.Status;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DriverSpecification {

    public static Specification<Driver> filterDrivers(
            Status status,
            String partnerId,
            Availability availability
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Status effectiveStatus = (status != null) ? status : Status.ACTIVE;
            predicates.add(criteriaBuilder.equal(root.get("status"), effectiveStatus));

            if (partnerId != null && !partnerId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("partnerId"), partnerId.trim()));
            }

            if (availability != null) {
                predicates.add(criteriaBuilder.equal(root.get("availability"), availability));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
