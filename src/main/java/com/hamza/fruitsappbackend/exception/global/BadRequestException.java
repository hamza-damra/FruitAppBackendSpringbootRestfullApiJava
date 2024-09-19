package com.hamza.fruitsappbackend.exception.global;
public class BadRequestException extends RuntimeException{

    public BadRequestException(String message) {
        super(message);
    }
}
