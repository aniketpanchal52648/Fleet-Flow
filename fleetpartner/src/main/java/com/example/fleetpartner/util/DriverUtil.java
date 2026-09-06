package com.example.fleetpartner.util;

import com.example.fleetpartner.dto.DriverDto;
import com.example.fleetpartner.entites.Availability;
import com.example.fleetpartner.entites.Driver;
import com.example.fleetpartner.entites.Status;

public class DriverUtil {

    public static DriverDto mapToDto(Driver driver) {
        if (driver == null) {
            return null;
        }
        return DriverDto.builder()
                .id(driver.getId())
                .driverId(driver.getDriverId())
                .partnerId(driver.getPartnerId())
                .keycloakId(driver.getKeycloakId())
                .name(driver.getName())
                .phoneNo(driver.getPhoneNo())
                .licencesNo(driver.getLicencesNo())
                .licencesExpiry(driver.getLicencesExpiry())
                .status(driver.getStatus())
                .availability(driver.getAvailability())
                .build();
    }

    public static Driver mapToEntity(DriverDto dto) {
        if (dto == null) {
            return null;
        }
        Driver driver = new Driver();
        if (dto.getId() != null) {
            driver.setId(dto.getId());
        }
        if (dto.getDriverId() != null && !dto.getDriverId().trim().isEmpty()) {
            driver.setDriverId(dto.getDriverId().trim());
        }
        if (dto.getPartnerId() != null) {
            driver.setPartnerId(dto.getPartnerId().trim());
        }
        driver.setKeycloakId(dto.getKeycloakId());
        driver.setName(dto.getName());
        if (dto.getPhoneNo() != null) {
            driver.setPhoneNo(dto.getPhoneNo().trim());
        }
        if (dto.getLicencesNo() != null) {
            driver.setLicencesNo(dto.getLicencesNo().trim());
        }
        driver.setLicencesExpiry(dto.getLicencesExpiry());
        driver.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.PENDING);
        driver.setAvailability(dto.getAvailability() != null ? dto.getAvailability() : Availability.AVAILABLE);
        return driver;
    }

    public static void copyDtoToEntity(DriverDto dto, Driver driver) {
        if (dto == null || driver == null) {
            return;
        }
        if (dto.getName() != null) {
            driver.setName(dto.getName());
        }
        if (dto.getKeycloakId() != null) {
            driver.setKeycloakId(dto.getKeycloakId());
        }
        if (dto.getPhoneNo() != null && !dto.getPhoneNo().trim().isEmpty()) {
            driver.setPhoneNo(dto.getPhoneNo().trim());
        }
        if (dto.getLicencesNo() != null && !dto.getLicencesNo().trim().isEmpty()) {
            driver.setLicencesNo(dto.getLicencesNo().trim());
        }
        if (dto.getLicencesExpiry() != null) {
            driver.setLicencesExpiry(dto.getLicencesExpiry());
        }
        if (dto.getPartnerId() != null) {
            driver.setPartnerId(dto.getPartnerId().trim().isEmpty() ? null : dto.getPartnerId().trim());
        }
        if (dto.getAvailability() != null) {
            driver.setAvailability(dto.getAvailability());
        }
        if (dto.getStatus() != null) {
            driver.setStatus(dto.getStatus());
        }
    }
}
