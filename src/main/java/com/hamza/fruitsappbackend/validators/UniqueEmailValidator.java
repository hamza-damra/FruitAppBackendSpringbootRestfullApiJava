package com.hamza.fruitsappbackend.validators;

import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.validators.annotation.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isEmpty()) {
            return true;
        }
        return !userRepository.existsByEmail(email);
    }
}
