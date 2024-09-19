package com.hamza.fruitsappbackend.exception.global;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CustomResponseStatusException extends RuntimeException {
    public CustomResponseStatusException(String message) {
        super(message);
    }
}
