package com.example.userservice.dto;

import com.example.userservice.entities.Status;
import com.example.userservice.entities.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private String id;
    private String userId;
    private String keycloakId;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Enter valid email")
    private String email;

    private String phoneNo;
    private String firstName;
    private String lastName;
    private UserRole role;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<UserAddressDto> userAddressDtoList;
}
