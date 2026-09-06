package com.example.fleetpartner.service.impl;

import com.example.fleetpartner.dto.ApiResponseDto;
import com.example.fleetpartner.dto.DriverDto;
import com.example.fleetpartner.entites.Availability;
import com.example.fleetpartner.entites.Driver;
import com.example.fleetpartner.entites.Status;
import com.example.fleetpartner.repository.DriverRepository;
import com.example.fleetpartner.repository.FleetPartnerRepository;
import com.example.fleetpartner.repository.spec.DriverSpecification;
import com.example.fleetpartner.service.DriverService;
import com.example.fleetpartner.util.DriverUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorDetail;
import org.example.error.ErrorException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final FleetPartnerRepository fleetPartnerRepository;

    @Override
    public DriverDto createDriver(DriverDto driverDto) {
        // 1. Validation first
        if (driverDto.getName() == null || driverDto.getName().trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("FP_DRIVER_NAME_REQUIRED", "name", "Driver name is required"));
        }

        if (driverDto.getPhoneNo() == null || driverDto.getPhoneNo().trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("FP_PHONE_NO_REQUIRED", "phoneNo", "Driver phone number is required"));
        }

        if (driverRepository.existsByPhoneNo(driverDto.getPhoneNo().trim())) {
            throw new ErrorException(new ErrorDetail("FP_PHONE_NO_EXISTS", "phoneNo",
                    "Driver with phone number already exists: " + driverDto.getPhoneNo()));
        }

        if (driverDto.getLicencesNo() == null || driverDto.getLicencesNo().trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("FP_LICENCE_NO_REQUIRED", "licencesNo", "Driver licence number is required"));
        }

        if (driverRepository.existsByLicencesNo(driverDto.getLicencesNo().trim())) {
            throw new ErrorException(new ErrorDetail("FP_LICENCE_NO_EXISTS", "licencesNo",
                    "Driver with licence number already exists: " + driverDto.getLicencesNo()));
        }

        if (driverDto.getPartnerId() != null && !driverDto.getPartnerId().trim().isEmpty()) {
            if (!fleetPartnerRepository.existsByPartnerId(driverDto.getPartnerId().trim())) {
                throw new ErrorException(new ErrorDetail("FP_PARTNER_NOT_FOUND", "partnerId",
                        "Fleet partner not found with partnerId: " + driverDto.getPartnerId()));
            }
        }

        // 2. Map DTO to Entity
        Driver driver = DriverUtil.mapToEntity(driverDto);
        if (driver.getDriverId() == null || driver.getDriverId().trim().isEmpty()) {
            driver.setDriverId("DRV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // 3. Save entity
        Driver saved = driverRepository.save(driver);
        log.info("Created driver with driverId: {}", saved.getDriverId());
        return DriverUtil.mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverDto> getDrivers(Status status, String partnerId, Availability availability) {
        Specification<Driver> spec = DriverSpecification.filterDrivers(status, partnerId, availability);
        List<Driver> drivers = driverRepository.findAll(spec);
        return drivers.stream().map(DriverUtil::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DriverDto getDriverByDriverId(String driverId) {
        Driver driver = findDriverOrThrow(driverId);
        return DriverUtil.mapToDto(driver);
    }

    @Override
    public DriverDto updateDriver(String driverId, DriverDto driverDto) {
        // 1. Validation first
        Driver driver = findDriverOrThrow(driverId);

        if (driverDto.getPhoneNo() != null && !driverDto.getPhoneNo().trim().isEmpty()) {
            String newPhone = driverDto.getPhoneNo().trim();
            if (!newPhone.equalsIgnoreCase(driver.getPhoneNo())) {
                if (driverRepository.existsByPhoneNo(newPhone)) {
                    throw new ErrorException(new ErrorDetail("FP_PHONE_NO_EXISTS", "phoneNo",
                            "Driver with phone number already exists: " + newPhone));
                }
            }
        }

        if (driverDto.getLicencesNo() != null && !driverDto.getLicencesNo().trim().isEmpty()) {
            String newLicence = driverDto.getLicencesNo().trim();
            if (!newLicence.equalsIgnoreCase(driver.getLicencesNo())) {
                if (driverRepository.existsByLicencesNo(newLicence)) {
                    throw new ErrorException(new ErrorDetail("FP_LICENCE_NO_EXISTS", "licencesNo",
                            "Driver with licence number already exists: " + newLicence));
                }
            }
        }

        if (driverDto.getPartnerId() != null && !driverDto.getPartnerId().trim().isEmpty()) {
            if (!fleetPartnerRepository.existsByPartnerId(driverDto.getPartnerId().trim())) {
                throw new ErrorException(new ErrorDetail("FP_PARTNER_NOT_FOUND", "partnerId",
                        "Fleet partner not found with partnerId: " + driverDto.getPartnerId()));
            }
        }

        // 2. Map updates
        DriverUtil.copyDtoToEntity(driverDto, driver);

        // 3. Save entity
        Driver updated = driverRepository.save(driver);
        log.info("Updated driver with driverId: {}", driverId);
        return DriverUtil.mapToDto(updated);
    }

    @Override
    public ApiResponseDto deleteDriver(String driverId) {
        Driver driver = findDriverOrThrow(driverId);
        driver.setStatus(Status.DELETED);
        driverRepository.save(driver);
        log.info("Soft deleted driver with driverId: {}", driverId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Driver deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponseDto activateDriver(String driverId) {
        Driver driver = findDriverOrThrow(driverId);
        driver.setStatus(Status.ACTIVE);
        driverRepository.save(driver);
        log.info("Activated driver with driverId: {}", driverId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Driver activated successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponseDto inactivateDriver(String driverId) {
        Driver driver = findDriverOrThrow(driverId);
        driver.setStatus(Status.INACTIVE);
        driverRepository.save(driver);
        log.info("Inactivated driver with driverId: {}", driverId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Driver set to inactive successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private Driver findDriverOrThrow(String driverId) {
        return driverRepository.findByDriverId(driverId)
                .orElseThrow(() -> new ErrorException(new ErrorDetail("FP_DRIVER_NOT_FOUND", "driverId",
                        "Driver not found with driverId: " + driverId)));
    }
}
