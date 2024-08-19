package com.hamza.fruitsappbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderNotFoundException extends RuntimeException {

    // Constructor for two arguments
    public OrderNotFoundException(String fieldName, String fieldValue) {
        super(String.format("Order not found with %s='%s'", fieldName, fieldValue));
    }

    public OrderNotFoundException(String orderField, String orderValue, String userField, String userValue) {
        super(String.format("Order not found with %s='%s' and %s='%s'", orderField, orderValue, userField, userValue));
    }
}
