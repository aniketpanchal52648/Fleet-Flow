package com.example.shipmentservice.util;

import com.example.shipmentservice.dto.UserAddressDto;
import com.example.shipmentservice.dto.UserShipmentDto;
import com.example.shipmentservice.entities.Shipment;
import com.example.shipmentservice.entities.UserAddress;

import java.util.ArrayList;
import java.util.List;

public class ShipmentServiceUtils {

    public static Shipment dtoToEntity(UserShipmentDto dto) {
        if (dto == null) {
            return null;
        }
        Shipment shipment = new Shipment();
        shipment.setId(dto.getId());
        shipment.setShipmentId(dto.getShipmentId());
        shipment.setUserId(dto.getUserId());
        if (dto.getPickupLocation() != null) {
            shipment.setPickupLocation(userAddDtoToEntity(dto.getPickupLocation()));
        }
        if (dto.getDropLocation() != null) {
            shipment.setDropLocation(userAddDtoToEntity(dto.getDropLocation()));
        }
        shipment.setShipmentStatus(dto.getShipmentStatus());
        shipment.setDriverId(dto.getDriverId());
        shipment.setVehicleId(dto.getVehicleId());
        shipment.setVehicleType(dto.getVehicleType());
        shipment.setWeightKg(dto.getWeightKg());
        shipment.setCancellationReason(dto.getCancellationReason());
        shipment.setPickupTime(dto.getPickupTime());
        shipment.setDeliveryTime(dto.getDeliveryTime());
        return shipment;
    }

    public static UserShipmentDto entityToDto(Shipment shipment) {
        if (shipment == null) {
            return null;
        }
        return UserShipmentDto.builder()
                .id(shipment.getId())
                .shipmentId(shipment.getShipmentId())
                .userId(shipment.getUserId())
                .pickupLocation(shipment.getPickupLocation() != null ? userAddEntityToDto(shipment.getPickupLocation()) : null)
                .dropLocation(shipment.getDropLocation() != null ? userAddEntityToDto(shipment.getDropLocation()) : null)
                .shipmentStatus(shipment.getShipmentStatus())
                .driverId(shipment.getDriverId())
                .vehicleId(shipment.getVehicleId())
                .vehicleType(shipment.getVehicleType())
                .weightKg(shipment.getWeightKg())
                .cancellationReason(shipment.getCancellationReason())
                .pickupTime(shipment.getPickupTime())
                .deliveryTime(shipment.getDeliveryTime())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }

    public static void copyDtoToEntity(UserShipmentDto dto, Shipment shipment) {
        if (dto == null || shipment == null) {
            return;
        }
        if (dto.getPickupLocation() != null) {
            shipment.setPickupLocation(userAddDtoToEntity(dto.getPickupLocation()));
        }
        if (dto.getDropLocation() != null) {
            shipment.setDropLocation(userAddDtoToEntity(dto.getDropLocation()));
        }
        if (dto.getVehicleType() != null) {
            shipment.setVehicleType(dto.getVehicleType());
        }
        if (dto.getWeightKg() != null) {
            shipment.setWeightKg(dto.getWeightKg());
        }
    }

    public static UserAddressDto userAddEntityToDto(UserAddress userAddress) {
        if (userAddress == null) {
            return null;
        }
        UserAddressDto userAddressDto = new UserAddressDto();
        userAddressDto.setAddressType(userAddress.getAddressType());
        userAddressDto.setUserId(userAddress.getUserId());
        userAddressDto.setDistrict(userAddress.getDistrict());
        userAddressDto.setState(userAddress.getState());
        userAddressDto.setAddressLineOne(userAddress.getAddressLineOne());
        userAddressDto.setAddressLineTwo(userAddress.getAddressLineTwo());
        userAddressDto.setGeoLocation(userAddress.getGeoLocation());
        userAddressDto.setPincode(userAddress.getPincode());
        userAddressDto.setId(userAddress.getId());
        return userAddressDto;
    }

    public static UserAddress userAddDtoToEntity(UserAddressDto userAddressDto) {
        if (userAddressDto == null) {
            return null;
        }
        UserAddress userAddress = new UserAddress();
        userAddress.setAddressType(userAddressDto.getAddressType());
        userAddress.setAddressLineOne(userAddressDto.getAddressLineOne());
        userAddress.setAddressLineTwo(userAddressDto.getAddressLineTwo());
        userAddress.setPincode(userAddressDto.getPincode());
        userAddress.setGeoLocation(userAddressDto.getGeoLocation());
        userAddress.setDistrict(userAddressDto.getDistrict());
        userAddress.setState(userAddressDto.getState());
        userAddress.setId(userAddressDto.getId());
        return userAddress;
    }

    public static List<UserShipmentDto> entityToDtoList(List<Shipment> shipmentList) {
        List<UserShipmentDto> userShipmentDtoList = new ArrayList<>();
        if (shipmentList != null) {
            for (Shipment shipment : shipmentList) {
                userShipmentDtoList.add(entityToDto(shipment));
            }
        }
        return userShipmentDtoList;
    }
}
