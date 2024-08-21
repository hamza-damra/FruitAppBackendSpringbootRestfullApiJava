package com.hamza.fruitsappbackend.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handle specific not found exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleResourceNotFound(ResourceNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleProductNotFound(ProductNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CartNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleCartNotFound(CartNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleCartItemNotFound(CartItemNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleCategoryNotFound(CategoryNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleOrderNotFound(OrderNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderItemNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleOrderItemNotFound(OrderItemNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleReviewNotFound(ReviewNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReviewImageNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleReviewImageNotFound(ReviewImageNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleRoleNotFound(RoleNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handle generic API exceptions
    @ExceptionHandler(FruitsApiException.class)
    protected ResponseEntity<CustomErrorResponse> handleFruitsApiException(FruitsApiException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getErrorMessage(),
                exception.getHttpStatus().value()
        );
        return new ResponseEntity<>(errorResponse, exception.getHttpStatus());
    }

    // Handle invalid total price exception
    @ExceptionHandler(InvalidTotalPriceException.class)
    protected ResponseEntity<CustomErrorResponse> handleInvalidTotalPriceException(InvalidTotalPriceException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle all other exceptions (catch-all)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<CustomErrorResponse> handleGlobalException(Exception exception) {
        // Log the exception (consider using a logger for this)
        exception.printStackTrace();  // Simple logging; replace with a logger in production
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                "An unexpected error occurred: " + exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle 404 errors for non-existing endpoints
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                   @NonNull HttpHeaders headers,
                                                                   @NonNull HttpStatusCode status,
                                                                   @NonNull WebRequest request) {
        String errorMessage = String.format("The URL %s does not exist", ex.getRequestURL());
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                errorMessage,
                HttpStatus.NOT_FOUND.value()
        );
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", errorResponse.getError());
        responseBody.put("code", errorResponse.getCode());
        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex,
                                                                              WebRequest request) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", "Access Denied");
        responseBody.put("code", HttpStatus.FORBIDDEN.value());

        return new ResponseEntity<>(responseBody, HttpStatus.FORBIDDEN);
    }


    // Handle validation errors
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<Map<String, String>> errorsList = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("field", error.getField());
                    errorMap.put("message", error.getDefaultMessage());
                    return errorMap;
                })
                .collect(Collectors.toList());

        String errorMessage = "Validation failed: " + errorsList.size() + " error(s) found";

        CustomErrorResponse errorResponse = new CustomErrorResponse(
                errorMessage,
                HttpStatus.BAD_REQUEST.value()
        );

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("errors", errorsList);
        responseBody.put("code", errorResponse.getCode());
        responseBody.put("message", errorResponse.getError());

        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }
}
