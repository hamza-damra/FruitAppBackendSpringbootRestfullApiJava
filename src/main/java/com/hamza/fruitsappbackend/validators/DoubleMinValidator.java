package com.hamza.fruitsappbackend.validators;

import com.hamza.fruitsappbackend.validators.annotation.DoubleMin;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DoubleMinValidator implements ConstraintValidator<DoubleMin, Double> {
    private double minValue;

    @Override
    public void initialize(DoubleMin constraintAnnotation) {
        this.minValue = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value >= minValue;
    }
}
