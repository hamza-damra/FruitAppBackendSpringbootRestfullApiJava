package com.hamza.fruitsappbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String fieldName, String fieldValue) {
        super(String.format("User not found with %s='%s'", fieldName, fieldValue));
    }
}
