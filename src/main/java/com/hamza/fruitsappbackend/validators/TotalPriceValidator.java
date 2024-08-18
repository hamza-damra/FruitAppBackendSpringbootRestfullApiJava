package com.hamza.fruitsappbackend.validators;

import com.hamza.fruitsappbackend.dto.OrderDTO;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.validators.annotation.ValidTotalPrice;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class TotalPriceValidator implements ConstraintValidator<ValidTotalPrice, OrderDTO> {

    @Autowired
    private ProductRepository productRepository;

    public boolean isValid(OrderDTO orderDTO, ConstraintValidatorContext context) {
        try {
            BigDecimal calculatedTotalPrice = BigDecimal.ZERO;

            for (var item : orderDTO.getOrderItems()) {
                if (item.getProductId() == null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Product ID must not be null")
                            .addPropertyNode("orderItems")
                            .addConstraintViolation();
                    return false;
                }

                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + item.getProductId()));

                BigDecimal itemTotalPrice = BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity()));
                calculatedTotalPrice = calculatedTotalPrice.add(itemTotalPrice);
            }

            if (calculatedTotalPrice.compareTo(orderDTO.getTotalPrice()) != 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                                "Provided total price does not match the calculated total price. Calculated: " +
                                        calculatedTotalPrice + ", Provided: " + orderDTO.getTotalPrice())
                        .addPropertyNode("totalPrice")
                        .addConstraintViolation();
                return false;
            }

            return true;

        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Unexpected validation error: " + e.getMessage())
                    .addConstraintViolation();
            return false;
        }
    }

}
