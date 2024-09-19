package com.hamza.fruitsappbackend.validation.annotation;

import com.hamza.fruitsappbackend.validation.validator.TotalPriceValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = TotalPriceValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTotalPrice {
    String message() default "Provided total price does not match the calculated total price.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
