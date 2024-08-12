package com.hamza.fruitsappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter

public class ErrorDetails {
    private Timestamp timestamp;
    private String message;
    private String details;

    public ErrorDetails(LocalDateTime localDateTime, String message, String details) {
        this.timestamp = Timestamp.valueOf(localDateTime)  ;
        this.message = message;
        this.details = details;
    }
}