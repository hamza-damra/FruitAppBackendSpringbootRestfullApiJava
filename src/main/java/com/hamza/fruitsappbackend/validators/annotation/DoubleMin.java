package com.hamza.fruitsappbackend.validators.annotation;

import com.hamza.fruitsappbackend.validators.DoubleMinValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DoubleMinValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleMin {
    String message() default "The value must be at least {value}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    double value();
}
