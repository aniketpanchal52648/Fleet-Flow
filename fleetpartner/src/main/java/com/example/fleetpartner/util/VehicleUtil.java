package com.example.fleetpartner.util;

import com.example.fleetpartner.dto.VehicleDto;
import com.example.fleetpartner.entites.Availability;
import com.example.fleetpartner.entites.Status;
import com.example.fleetpartner.entites.Vehicle;

public class VehicleUtil {

    public static VehicleDto mapToDto(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return VehicleDto.builder()
                .id(vehicle.getId())
                .vehicleId(vehicle.getVehicleId())
                .fleetPartnerId(vehicle.getFleetPartner() != null ? vehicle.getFleetPartner().getPartnerId() : null)
                .registrationNo(vehicle.getRegistrationNo())
                .vehicleType(vehicle.getVehicleType())
                .weightCapacity(vehicle.getWeightCapacity())
                .volumeCapacity(vehicle.getVolumeCapacity())
                .dimension(vehicle.getDimension())
                .status(vehicle.getStatus())
                .availability(vehicle.getAvailability())
                .build();
    }

    public static Vehicle mapToEntity(VehicleDto vehicleDto) {
        if (vehicleDto == null) {
            return null;
        }
        Vehicle vehicle = new Vehicle();
        if (vehicleDto.getId() != null) {
            vehicle.setId(vehicleDto.getId());
        }
        if (vehicleDto.getVehicleId() != null && !vehicleDto.getVehicleId().trim().isEmpty()) {
            vehicle.setVehicleId(vehicleDto.getVehicleId().trim());
        }
        if (vehicleDto.getRegistrationNo() != null) {
            vehicle.setRegistrationNo(vehicleDto.getRegistrationNo().trim());
        }
        vehicle.setVehicleType(vehicleDto.getVehicleType());
        vehicle.setWeightCapacity(vehicleDto.getWeightCapacity() != null ? vehicleDto.getWeightCapacity() : 0.0);
        vehicle.setVolumeCapacity(vehicleDto.getVolumeCapacity() != null ? vehicleDto.getVolumeCapacity() : 0.0);
        vehicle.setDimension(vehicleDto.getDimension());
        vehicle.setStatus(vehicleDto.getStatus() != null ? vehicleDto.getStatus() : Status.PENDING);
        vehicle.setAvailability(vehicleDto.getAvailability() != null ? vehicleDto.getAvailability() : Availability.AVAILABLE);
        return vehicle;
    }

    public static void copyDtoToEntity(VehicleDto vehicleDto, Vehicle vehicle) {
        if (vehicleDto == null || vehicle == null) {
            return;
        }
        if (vehicleDto.getRegistrationNo() != null && !vehicleDto.getRegistrationNo().trim().isEmpty()) {
            vehicle.setRegistrationNo(vehicleDto.getRegistrationNo().trim());
        }
        if (vehicleDto.getVehicleType() != null) {
            vehicle.setVehicleType(vehicleDto.getVehicleType());
        }
        if (vehicleDto.getWeightCapacity() != null) {
            vehicle.setWeightCapacity(vehicleDto.getWeightCapacity());
        }
        if (vehicleDto.getVolumeCapacity() != null) {
            vehicle.setVolumeCapacity(vehicleDto.getVolumeCapacity());
        }
        if (vehicleDto.getDimension() != null) {
            vehicle.setDimension(vehicleDto.getDimension());
        }
        if (vehicleDto.getAvailability() != null) {
            vehicle.setAvailability(vehicleDto.getAvailability());
        }
        if (vehicleDto.getStatus() != null) {
            vehicle.setStatus(vehicleDto.getStatus());
        }
    }
}
