package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
public class ErrorDetailCollection {
    Integer status;
    String message;
    LocalDateTime timestamp;
    List<ErrorDetail> errors;

    public ErrorDetailCollection(Integer status, String message, LocalDateTime timestamp, List<ErrorDetail> errors) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.errors = errors;
    }
}
