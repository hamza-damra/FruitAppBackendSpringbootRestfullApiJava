package com.hamza.fruitsappbackend.validators.annotation;

import com.hamza.fruitsappbackend.validators.DoubleMaxValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DoubleMaxValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleMax {
    String message() default "The value must be at most {value}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    double value();
}
