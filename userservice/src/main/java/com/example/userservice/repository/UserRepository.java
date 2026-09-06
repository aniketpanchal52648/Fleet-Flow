package com.example.userservice.repository;

import com.example.userservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByPhoneNo(String phoneNo);

    boolean existsByPhoneNo(String phoneNo);
}
