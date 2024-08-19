package com.hamza.fruitsappbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String fieldName, String fieldValue) {
        super(String.format("Address not found with %s='%s'", fieldName, fieldValue));
    }
}
