package com.hamza.fruitsappbackend.security.dto;

import com.hamza.fruitsappbackend.modules.user.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JwtAuthResponseDtoSignup {
    private String token;
    private String type = "Bearer";
    private UserDTO createdUser;

    public JwtAuthResponseDtoSignup(UserDTO createdUser, String token) {
        this.createdUser = createdUser;
        this.token = token;
    }

}