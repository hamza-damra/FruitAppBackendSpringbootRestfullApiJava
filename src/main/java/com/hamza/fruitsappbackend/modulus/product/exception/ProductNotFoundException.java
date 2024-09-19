package com.hamza.fruitsappbackend.modulus.product.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String fieldName, String fieldValue) {
        super(String.format("Product not found with %s='%s'", fieldName, fieldValue));
    }
}
