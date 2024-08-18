package com.hamza.fruitsappbackend.validators;

import com.hamza.fruitsappbackend.dto.OrderDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class TotalPriceValidator implements ConstraintValidator<ValidTotalPrice, OrderDTO> {

    @Override
    public boolean isValid(OrderDTO orderDTO, ConstraintValidatorContext context) {
        // The price in OrderItemDTO already accounts for quantity, so no need to multiply
        BigDecimal calculatedTotalPrice = orderDTO.getOrderItems().stream()
                .map(item -> BigDecimal.valueOf(item.getPrice()))  // Use price directly
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Check if the calculated total price matches the provided total price
        if (calculatedTotalPrice.compareTo(orderDTO.getTotalPrice()) != 0) {
            // Disable default constraint violation
            context.disableDefaultConstraintViolation();
            // Add a custom message to the constraint violation for the totalPrice field
            context.buildConstraintViolationWithTemplate(
                            "Provided total price does not match the calculated total price. " +
                                    "Calculated: " + calculatedTotalPrice + ", Provided: " + orderDTO.getTotalPrice())
                    .addPropertyNode("totalPrice")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
