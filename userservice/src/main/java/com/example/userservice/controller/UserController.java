package com.example.userservice.controller;

import com.example.userservice.dto.ApiResponseDto;
import com.example.userservice.dto.UserAddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entities.Status;
import com.example.userservice.entities.UserRole;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("web/v1/user-service/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserDto created = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNo
    ) {
        List<UserDto> users = userService.getUsers(status, role, email, phoneNo);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String userId,
            @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(userId, userDto));
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponseDto> activateUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.activateUser(userId));
    }

    @PatchMapping("/{userId}/inactivate")
    public ResponseEntity<ApiResponseDto> inactivateUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.inactivateUser(userId));
    }

    @PatchMapping("/{userId}/delete")
    public ResponseEntity<ApiResponseDto> deleteUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    // Address sub-resources
    @PostMapping("/{userId}/address")
    public ResponseEntity<UserAddressDto> addAddress(
            @PathVariable String userId,
            @RequestBody UserAddressDto addressDto) {
        UserAddressDto created = userService.addAddress(userId, addressDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<UserAddressDto>> getUserAddresses(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserAddresses(userId));
    }

    @PutMapping("/{userId}/address/{addressId}")
    public ResponseEntity<UserAddressDto> updateAddress(
            @PathVariable String userId,
            @PathVariable String addressId,
            @RequestBody UserAddressDto addressDto) {
        return ResponseEntity.ok(userService.updateAddress(userId, addressId, addressDto));
    }

    @DeleteMapping("/{userId}/address/{addressId}")
    public ResponseEntity<ApiResponseDto> deleteAddress(
            @PathVariable String userId,
            @PathVariable String addressId) {
        return ResponseEntity.ok(userService.deleteAddress(userId, addressId));
    }
}
