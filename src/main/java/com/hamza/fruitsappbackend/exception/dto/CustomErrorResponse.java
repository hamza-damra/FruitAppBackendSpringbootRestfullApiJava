package com.hamza.fruitsappbackend.exception.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomErrorResponse {

    private String error;
    private int code;

    public CustomErrorResponse(String error, int code) {
        this.error = error;
        this.code = code;
    }
}
