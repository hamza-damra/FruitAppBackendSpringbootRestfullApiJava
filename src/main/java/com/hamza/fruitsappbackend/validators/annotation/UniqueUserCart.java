package com.hamza.fruitsappbackend.validators.annotation;

import com.hamza.fruitsappbackend.validators.UniqueUserCartValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueUserCartValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUserCart {
    String message() default "User can have only one cart";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
