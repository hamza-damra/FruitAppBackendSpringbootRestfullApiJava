package com.hamza.fruitsappbackend.exception;

import com.hamza.fruitsappbackend.modules.cart.exception.CartItemNotFoundException;
import com.hamza.fruitsappbackend.modules.cart.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.modules.cart.exception.InsufficientStockException;
import com.hamza.fruitsappbackend.modules.cart.exception.InvalidTotalPriceException;
import com.hamza.fruitsappbackend.exception.dto.CustomErrorResponse;
import com.hamza.fruitsappbackend.exception.global.BadRequestException;
import com.hamza.fruitsappbackend.exception.global.CustomResponseStatusException;
import com.hamza.fruitsappbackend.exception.global.FruitsApiException;
import com.hamza.fruitsappbackend.exception.global.ResourceNotFoundException;
import com.hamza.fruitsappbackend.modules.order.exception.OrderItemNotFoundException;
import com.hamza.fruitsappbackend.modules.order.exception.OrderNotFoundException;
import com.hamza.fruitsappbackend.modules.product.exception.CategoryNotFoundException;
import com.hamza.fruitsappbackend.modules.product.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.modules.review.exception.ReviewImageNotFoundException;
import com.hamza.fruitsappbackend.modules.review.exception.ReviewNotFoundException;
import com.hamza.fruitsappbackend.modules.role.exception.RoleNotFoundException;
import com.hamza.fruitsappbackend.modules.user.exception.UserNotFoundException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleResourceNotFound(ResourceNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ProductNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleProductNotFound(ProductNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(CartNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleCartNotFound(CartNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(CartItemNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleCartItemNotFound(CartItemNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(CategoryNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleCategoryNotFound(CategoryNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(OrderNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleOrderNotFound(OrderNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(OrderItemNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleOrderItemNotFound(OrderItemNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ReviewNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleReviewNotFound(ReviewNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(CustomResponseStatusException.class)
    protected ResponseEntity<CustomErrorResponse> handleCustomResponseStatusException(CustomResponseStatusException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ReviewImageNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleReviewImageNotFound(ReviewImageNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RoleNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleRoleNotFound(RoleNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<CustomErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(FruitsApiException.class)
    protected ResponseEntity<CustomErrorResponse> handleFruitsApiException(FruitsApiException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getErrorMessage(),
                exception.getHttpStatus().value()
        );
        return new ResponseEntity<>(errorResponse, exception.getHttpStatus());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidTotalPriceException.class)
    protected ResponseEntity<CustomErrorResponse> handleInvalidTotalPriceException(InvalidTotalPriceException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }



    @org.springframework.web.bind.annotation.ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<CustomErrorResponse> handleBadRequestException(BadRequestException exception)
    {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                exception.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MissingRequestHeaderException.class)
    protected ResponseEntity<CustomErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                "Missing required request header: " + ex.getHeaderName(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Resolved [io.jsonwebtoken.security.SignatureException: JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.]
    @org.springframework.web.bind.annotation.ExceptionHandler(SignatureException.class)
    protected ResponseEntity<CustomErrorResponse> handleSignatureException(SignatureException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                "Invalid JWT signature. JWT validity cannot be asserted and should not be trusted.",
                HttpStatus.FORBIDDEN.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }


    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    protected ResponseEntity<CustomErrorResponse> handleGlobalException(Exception exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                "An unexpected error occurred: " + exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

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

    @org.springframework.web.bind.annotation.ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", ex.getMessage());
        responseBody.put("code", HttpStatus.FORBIDDEN.value());

        return new ResponseEntity<>(responseBody, HttpStatus.FORBIDDEN);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull  WebRequest request) {
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

    @org.springframework.web.bind.annotation.ExceptionHandler(InsufficientStockException.class)
    protected ResponseEntity<CustomErrorResponse> handleInsufficientStockException(InsufficientStockException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ex.getMessage() + " is not available in the stock",
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
