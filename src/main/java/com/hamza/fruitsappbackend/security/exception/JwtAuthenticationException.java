package com.hamza.fruitsappbackend.security.exception;

import org.springframework.security.core.AuthenticationException;
public class JwtAuthenticationException extends AuthenticationException {
    public JwtAuthenticationException(String msg) {
        super(msg);
    }

    public JwtAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}