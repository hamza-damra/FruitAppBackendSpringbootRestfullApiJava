package com.hamza.fruitsappbackend.validators;

import com.hamza.fruitsappbackend.repository.CartRepository;
import com.hamza.fruitsappbackend.validators.annotation.UniqueUserCart;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueUserCartValidator implements ConstraintValidator<UniqueUserCart, Long> {

    @Autowired
    private CartRepository cartRepository;

    @Override
    public void initialize(UniqueUserCart constraintAnnotation) {
    }

    @Override
    public boolean isValid(Long userId, ConstraintValidatorContext context) {
        if (userId == null) {
            return true; // Null values can be handled by @NotNull if required
        }
        return cartRepository.findByUserId(userId).isEmpty();
    }
}
