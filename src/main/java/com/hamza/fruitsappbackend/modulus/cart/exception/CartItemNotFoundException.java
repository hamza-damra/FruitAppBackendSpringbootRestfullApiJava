package com.hamza.fruitsappbackend.modulus.cart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(String fieldName, String fieldValue) {
        super(String.format("CartItem not found with %s='%s'", fieldName, fieldValue));
    }
}
