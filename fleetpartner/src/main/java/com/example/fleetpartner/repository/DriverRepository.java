package com.example.fleetpartner.repository;

import com.example.fleetpartner.entites.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String>, JpaSpecificationExecutor<Driver> {

    Optional<Driver> findByDriverId(String driverId);

    boolean existsByDriverId(String driverId);

    boolean existsByPhoneNo(String phoneNo);

    boolean existsByLicencesNo(String licencesNo);
}
