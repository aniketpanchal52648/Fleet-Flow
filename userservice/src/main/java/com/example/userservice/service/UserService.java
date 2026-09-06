package com.example.userservice.service;

import com.example.userservice.dto.ApiResponseDto;
import com.example.userservice.dto.UserAddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entities.Status;
import com.example.userservice.entities.User;
import com.example.userservice.entities.UserAddress;
import com.example.userservice.entities.UserRole;
import com.example.userservice.repository.UserAddressRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.spec.UserSpecification;
import com.example.userservice.util.UserServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorDetail;
import org.example.error.ErrorException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("USER_EMAIL_REQUIRED", "email", "Email cannot be blank"));
        }

        String email = userDto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ErrorException(new ErrorDetail("USER_EMAIL_EXISTS", "email", "User with email already exists: " + email));
        }

        if (userDto.getPhoneNo() == null || userDto.getPhoneNo().trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("USER_PHONE_REQUIRED", "phoneNo", "Phone number cannot be blank"));
        }

        String phoneNo = userDto.getPhoneNo().trim();
        if (userRepository.existsByPhoneNo(phoneNo)) {
            throw new ErrorException(new ErrorDetail("USER_PHONE_EXISTS", "phoneNo", "User with phone number already exists: " + phoneNo));
        }

        if (userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty()) {
            throw new ErrorException(new ErrorDetail("USER_FIRST_NAME_REQUIRED", "firstName", "First name cannot be blank"));
        }

        User user = UserServiceUtils.userDtoToEntity(userDto);
        user.setEmail(email);
        user.setPhoneNo(phoneNo);

        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            user.setUserId("USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        if (user.getStatus() == null) {
            user.setStatus(Status.PENDING);
        }

        if (user.getRole() == null) {
            user.setRole(UserRole.USER_ROLE);
        }

        User createdUser = userRepository.save(user);
        log.info("Created user with userId: {}", createdUser.getUserId());
        return UserServiceUtils.entityToDto(createdUser);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(String userId) {
        User user = findUserOrThrow(userId);
        return UserServiceUtils.entityToDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsers(Status status, UserRole role, String email, String phoneNo) {
        Specification<User> spec = UserSpecification.filterUsers(status, role, email, phoneNo);
        List<User> users = userRepository.findAll(spec);
        return users.stream().map(UserServiceUtils::entityToDto).collect(Collectors.toList());
    }

    public UserDto updateUser(String userId, UserDto userDto) {
        User user = findUserOrThrow(userId);

        if (userDto.getPhoneNo() != null && !userDto.getPhoneNo().trim().isEmpty()) {
            String newPhone = userDto.getPhoneNo().trim();
            if (!newPhone.equalsIgnoreCase(user.getPhoneNo())) {
                if (userRepository.existsByPhoneNo(newPhone)) {
                    throw new ErrorException(new ErrorDetail("USER_PHONE_EXISTS", "phoneNo", "User with phone number already exists: " + newPhone));
                }
            }
        }

        UserServiceUtils.copyDtoToEntity(userDto, user);

        if (userDto.getUserAddressDtoList() != null) {
            user.getUserAddressList().clear();
            for (UserAddressDto addressDto : userDto.getUserAddressDtoList()) {
                user.addAddress(UserServiceUtils.userAddDtoToEntity(addressDto));
            }
        }

        User updated = userRepository.save(user);
        log.info("Updated user profile for userId: {}", userId);
        return UserServiceUtils.entityToDto(updated);
    }

    public ApiResponseDto activateUser(String userId) {
        User user = findUserOrThrow(userId);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        log.info("Activated user with userId: {}", userId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("User activated successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ApiResponseDto inactivateUser(String userId) {
        User user = findUserOrThrow(userId);
        user.setStatus(Status.INACTIVE);
        userRepository.save(user);
        log.info("Inactivated user with userId: {}", userId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("User set to inactive successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ApiResponseDto deleteUser(String userId) {
        User user = findUserOrThrow(userId);
        user.setStatus(Status.DELETED);
        userRepository.save(user);
        log.info("Soft deleted user with userId: {}", userId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("User deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Address sub-resource management
    public UserAddressDto addAddress(String userId, UserAddressDto addressDto) {
        User user = findUserOrThrow(userId);
        UserAddress address = UserServiceUtils.userAddDtoToEntity(addressDto);
        user.addAddress(address);
        userRepository.save(user);
        log.info("Added address to user: {}", userId);
        return UserServiceUtils.userAddEntityToDto(address);
    }

    @Transactional(readOnly = true)
    public List<UserAddressDto> getUserAddresses(String userId) {
        User user = findUserOrThrow(userId);
        return UserServiceUtils.userAddressEntityToDtoList(user.getUserAddressList());
    }

    public UserAddressDto updateAddress(String userId, String addressId, UserAddressDto addressDto) {
        User user = findUserOrThrow(userId);
        UserAddress address = user.getUserAddressList().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ErrorException(new ErrorDetail("ADDRESS_NOT_FOUND", "addressId", "Address not found for user: " + addressId)));

        UserServiceUtils.copyAddressDtoToEntity(addressDto, address);
        userRepository.save(user);
        log.info("Updated address {} for user {}", addressId, userId);
        return UserServiceUtils.userAddEntityToDto(address);
    }

    public ApiResponseDto deleteAddress(String userId, String addressId) {
        User user = findUserOrThrow(userId);
        UserAddress address = user.getUserAddressList().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ErrorException(new ErrorDetail("ADDRESS_NOT_FOUND", "addressId", "Address not found for user: " + addressId)));

        user.removeAddress(address);
        userRepository.save(user);
        log.info("Deleted address {} for user {}", addressId, userId);
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message("Address deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private User findUserOrThrow(String userId) {
        return userRepository.findByUserId(userId)
                .or(() -> userRepository.findById(userId))
                .orElseThrow(() -> new ErrorException(new ErrorDetail("USER_NOT_FOUND", "userId", "User not found with id: " + userId)));
    }
}
