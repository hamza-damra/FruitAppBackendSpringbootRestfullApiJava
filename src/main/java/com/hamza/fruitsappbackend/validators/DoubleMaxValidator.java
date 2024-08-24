package com.hamza.fruitsappbackend.validators;

import com.hamza.fruitsappbackend.validators.annotation.DoubleMax;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DoubleMaxValidator implements ConstraintValidator<DoubleMax, Double> {
    private double maxValue;

    @Override
    public void initialize(DoubleMax constraintAnnotation) {
        this.maxValue = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value <= maxValue;
    }
}
