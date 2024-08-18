package com.hamza.fruitsappbackend.exception;

import org.springframework.http.HttpStatus;

public class InvalidTotalPriceException extends RuntimeException {

    private final HttpStatus status;

    public InvalidTotalPriceException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
