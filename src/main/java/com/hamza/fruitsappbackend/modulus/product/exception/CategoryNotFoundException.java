package com.hamza.fruitsappbackend.modulus.product.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String fieldName, String fieldValue) {
        super(String.format("Category not found with %s='%s'", fieldName, fieldValue));
    }
}
