package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class ErrorDetail {
    private String code;

    private String field;
    private String message;

    public String getCode() {
        return code;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public ErrorDetail(String code, String field, String message) {
        this.code = code;
        this.field = field;
        this.message = message;
    }
}
