package com.example.userservice.repository;

import com.example.userservice.entities.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, String> {

    List<UserAddress> findByUser_UserId(String userId);

    List<UserAddress> findByUser_Id(String id);

    Optional<UserAddress> findByIdAndUser_UserId(String id, String userId);

    Optional<UserAddress> findByIdAndUser_Id(String id, String userId);
}
