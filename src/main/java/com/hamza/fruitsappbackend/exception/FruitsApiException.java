package com.hamza.fruitsappbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class FruitsApiException extends RuntimeException {
    private HttpStatus httpStatus;
    private String errorMessage;
    private String errorMessageDetails;

    public FruitsApiException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public FruitsApiException(String errorMessage) {
        super(errorMessage);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorMessage = errorMessage;
    }
}
