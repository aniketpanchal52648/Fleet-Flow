package org.example.error;

import org.example.dto.ErrorDetail;

public class ErrorException extends RuntimeException{
    private final ErrorDetail errorDetail;

    public ErrorException(ErrorDetail errorDetail) {
        this.errorDetail = errorDetail;
    }

    public ErrorDetail getErrorDetail(){
        return errorDetail;
    }
}
