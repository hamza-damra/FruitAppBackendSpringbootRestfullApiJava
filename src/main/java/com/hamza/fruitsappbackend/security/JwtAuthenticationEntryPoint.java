package com.hamza.fruitsappbackend.security;

import com.hamza.fruitsappbackend.exception.dto.CustomErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        CustomErrorResponse customErrorResponse = new CustomErrorResponse("Unauthorized: Invalid JWT token. Please log in again.", 401);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, String.valueOf(customErrorResponse));
    }
}