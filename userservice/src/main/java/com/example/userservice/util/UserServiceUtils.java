package com.example.userservice.util;

import com.example.userservice.dto.UserAddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entities.Status;
import com.example.userservice.entities.User;
import com.example.userservice.entities.UserAddress;
import com.example.userservice.entities.UserRole;

import java.util.ArrayList;
import java.util.List;

public class UserServiceUtils {

    public static User userDtoToEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        User user = new User();
        user.setId(userDto.getId());
        user.setUserId(userDto.getUserId());
        user.setKeycloakId(userDto.getKeycloakId());
        user.setEmail(userDto.getEmail() != null ? userDto.getEmail().trim() : null);
        user.setPhoneNo(userDto.getPhoneNo() != null ? userDto.getPhoneNo().trim() : null);
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setRole(userDto.getRole() != null ? userDto.getRole() : UserRole.USER_ROLE);
        user.setStatus(userDto.getStatus() != null ? userDto.getStatus() : Status.PENDING);

        if (userDto.getUserAddressDtoList() != null) {
            for (UserAddressDto dto : userDto.getUserAddressDtoList()) {
                user.addAddress(userAddDtoToEntity(dto));
            }
        }
        return user;
    }

    public static UserDto entityToDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .phoneNo(user.getPhoneNo())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .userAddressDtoList(userAddressEntityToDtoList(user.getUserAddressList()))
                .build();
    }

    public static void copyDtoToEntity(UserDto dto, User user) {
        if (dto == null || user == null) {
            return;
        }
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getPhoneNo() != null && !dto.getPhoneNo().trim().isEmpty()) {
            user.setPhoneNo(dto.getPhoneNo().trim());
        }
        if (dto.getKeycloakId() != null) {
            user.setKeycloakId(dto.getKeycloakId());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
    }

    public static UserAddress userAddDtoToEntity(UserAddressDto dto) {
        if (dto == null) {
            return null;
        }
        UserAddress userAddress = new UserAddress();
        userAddress.setId(dto.getId());
        userAddress.setAddressType(dto.getAddressType());
        userAddress.setAddressLineOne(dto.getAddressLineOne());
        userAddress.setAddressLineTwo(dto.getAddressLineTwo());
        userAddress.setPincode(dto.getPincode());
        userAddress.setGeoLocation(dto.getGeoLocation());
        userAddress.setDistrict(dto.getDistrict());
        userAddress.setState(dto.getState());
        return userAddress;
    }

    public static UserAddressDto userAddEntityToDto(UserAddress userAddress) {
        if (userAddress == null) {
            return null;
        }
        String ownerUserId = null;
        if (userAddress.getUser() != null) {
            ownerUserId = userAddress.getUser().getUserId() != null
                    ? userAddress.getUser().getUserId()
                    : userAddress.getUser().getId();
        }
        return UserAddressDto.builder()
                .id(userAddress.getId())
                .userId(ownerUserId)
                .addressType(userAddress.getAddressType())
                .district(userAddress.getDistrict())
                .state(userAddress.getState())
                .addressLineOne(userAddress.getAddressLineOne())
                .addressLineTwo(userAddress.getAddressLineTwo())
                .geoLocation(userAddress.getGeoLocation())
                .pincode(userAddress.getPincode())
                .build();
    }

    public static void copyAddressDtoToEntity(UserAddressDto dto, UserAddress entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getAddressType() != null) {
            entity.setAddressType(dto.getAddressType());
        }
        if (dto.getAddressLineOne() != null) {
            entity.setAddressLineOne(dto.getAddressLineOne());
        }
        if (dto.getAddressLineTwo() != null) {
            entity.setAddressLineTwo(dto.getAddressLineTwo());
        }
        if (dto.getPincode() != null) {
            entity.setPincode(dto.getPincode());
        }
        if (dto.getDistrict() != null) {
            entity.setDistrict(dto.getDistrict());
        }
        if (dto.getState() != null) {
            entity.setState(dto.getState());
        }
        if (dto.getGeoLocation() != null) {
            entity.setGeoLocation(dto.getGeoLocation());
        }
    }

    public static List<UserAddressDto> userAddressEntityToDtoList(List<UserAddress> userAddressList) {
        List<UserAddressDto> userAddressDtoList = new ArrayList<>();
        if (userAddressList != null) {
            for (UserAddress userAddress : userAddressList) {
                userAddressDtoList.add(userAddEntityToDto(userAddress));
            }
        }
        return userAddressDtoList;
    }
}
