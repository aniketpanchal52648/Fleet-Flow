package com.example.userservice.repository.spec;

import com.example.userservice.entities.Status;
import com.example.userservice.entities.User;
import com.example.userservice.entities.UserRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filterUsers(
            Status status,
            UserRole role,
            String email,
            String phoneNo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Status effectiveStatus = (status != null) ? status : Status.ACTIVE;
            predicates.add(criteriaBuilder.equal(root.get("status"), effectiveStatus));

            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            if (email != null && !email.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("email")),
                        email.trim().toLowerCase()
                ));
            }

            if (phoneNo != null && !phoneNo.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("phoneNo"), phoneNo.trim()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
