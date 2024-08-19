package com.hamza.fruitsappbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String fieldName, String fieldValue) {
        super(String.format("Role not found with %s='%s'", fieldName, fieldValue));
    }
}
