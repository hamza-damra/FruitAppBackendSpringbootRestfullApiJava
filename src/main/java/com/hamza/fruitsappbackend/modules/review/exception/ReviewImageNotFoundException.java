package com.hamza.fruitsappbackend.modules.review.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReviewImageNotFoundException extends RuntimeException {
    public ReviewImageNotFoundException(String fieldName, String fieldValue) {
        super(String.format("ReviewImage not found with %s='%s'", fieldName, fieldValue));
    }
}
