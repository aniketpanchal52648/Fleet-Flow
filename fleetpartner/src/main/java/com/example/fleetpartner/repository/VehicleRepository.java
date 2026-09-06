package com.example.fleetpartner.repository;

import com.example.fleetpartner.entites.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String>, JpaSpecificationExecutor<Vehicle> {

    Optional<Vehicle> findByVehicleId(String vehicleId);

    boolean existsByVehicleId(String vehicleId);

    Optional<Vehicle> findByRegistrationNo(String registrationNo);

    boolean existsByRegistrationNo(String registrationNo);

    List<Vehicle> findByVehicleIdIn(List<String> vehicleIds);
}
