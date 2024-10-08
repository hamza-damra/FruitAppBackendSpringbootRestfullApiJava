package com.hamza.fruitsappbackend.exception.global;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
@Setter
@NoArgsConstructor
public class ResourceNotFoundException extends RuntimeException  {
    private  String resourceName;
    private  String fieldName;
    private  String fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s not found with %s='%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

}