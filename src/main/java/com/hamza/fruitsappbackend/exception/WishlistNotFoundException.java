package com.hamza.fruitsappbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WishlistNotFoundException extends RuntimeException {
    public WishlistNotFoundException(String fieldName, String fieldValue) {
        super(String.format("Wishlist not found with %s='%s'", fieldName, fieldValue));
    }
}
