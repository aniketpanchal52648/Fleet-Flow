package com.example.fleetpartner.repository;

import com.example.fleetpartner.entites.DriverVehicleAssignment;
import com.example.fleetpartner.entites.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverVehicleAssignmentRepository extends JpaRepository<DriverVehicleAssignment, String>, JpaSpecificationExecutor<DriverVehicleAssignment> {

    Optional<DriverVehicleAssignment> findByAssignmentId(String assignmentId);

    Optional<DriverVehicleAssignment> findByVehicleIdAndStatus(String vehicleId, Status status);

    Optional<DriverVehicleAssignment> findByDriverIdAndStatus(String driverId, Status status);

    boolean existsByVehicleIdAndStatus(String vehicleId, Status status);

    boolean existsByDriverIdAndStatus(String driverId, Status status);
}
