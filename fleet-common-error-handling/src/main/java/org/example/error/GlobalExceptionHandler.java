package org.example.error;

import org.example.dto.ErrorDetail;
import org.example.dto.ErrorDetailCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CollectionErrorException.class)
    public ResponseEntity<ErrorDetailCollection> handleBusinessValidation(
            CollectionErrorException ex) {

        ErrorDetailCollection response = new ErrorDetailCollection(
                HttpStatus.BAD_REQUEST.value(),
                "Business validation failed",
                LocalDateTime.now(),
                ex.getErrors()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ErrorDetail> handleSingleBusinessValidation(
            ErrorException ex
    ){
        ErrorDetail response=new ErrorDetail(ex.getErrorDetail().getCode(),ex.getErrorDetail().getField(),ex.getErrorDetail().getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
