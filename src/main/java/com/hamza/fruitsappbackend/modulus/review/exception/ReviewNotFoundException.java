package com.hamza.fruitsappbackend.modulus.review.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String fieldName, String fieldValue) {
        super(String.format("Review not found with %s='%s'", fieldName, fieldValue));
    }
}