package org.example.error;

import org.example.dto.ErrorDetail;

import java.util.List;

public class CollectionErrorException extends RuntimeException{
    private final List<ErrorDetail> errors;

    public CollectionErrorException(List<ErrorDetail> errors) {
        super("Validation issue");
        this.errors = errors;
    }
    public List<ErrorDetail> getErrors(){
        return errors;
    }
}
