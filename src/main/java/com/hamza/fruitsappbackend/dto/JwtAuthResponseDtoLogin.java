package com.hamza.fruitsappbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JwtAuthResponseDtoLogin {
    private String token;
    private String type = "Bearer";

    public JwtAuthResponseDtoLogin(String token) {
        this.token = token;
    }

}