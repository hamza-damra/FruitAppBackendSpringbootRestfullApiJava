package com.hamza.fruitsappbackend.modules.cart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String fieldName, String fieldValue) {
        super(String.format("Cart not found with %s='%s'", fieldName, fieldValue));
    }
}
