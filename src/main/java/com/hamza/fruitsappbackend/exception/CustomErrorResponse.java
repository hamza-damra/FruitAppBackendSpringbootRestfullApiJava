package com.hamza.fruitsappbackend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

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
