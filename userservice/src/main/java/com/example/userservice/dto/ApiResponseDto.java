package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDto {

    private String status;
    private String message;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
