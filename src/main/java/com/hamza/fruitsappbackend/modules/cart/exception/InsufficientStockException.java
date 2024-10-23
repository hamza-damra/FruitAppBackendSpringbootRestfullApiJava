package com.hamza.fruitsappbackend.modules.cart.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
        HttpStatus status = HttpStatus.BAD_REQUEST;
    }

}
