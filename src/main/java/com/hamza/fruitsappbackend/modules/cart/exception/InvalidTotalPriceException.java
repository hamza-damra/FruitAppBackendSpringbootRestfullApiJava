package com.hamza.fruitsappbackend.modules.cart.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidTotalPriceException extends RuntimeException {

    private final HttpStatus status;

    public InvalidTotalPriceException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

}
