package com.hamza.fruitsappbackend.modulus.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderItemNotFoundException extends RuntimeException {
    public OrderItemNotFoundException(String fieldName, String fieldValue) {
        super(String.format("OrderItem not found with %s='%s'", fieldName, fieldValue));
    }
}
