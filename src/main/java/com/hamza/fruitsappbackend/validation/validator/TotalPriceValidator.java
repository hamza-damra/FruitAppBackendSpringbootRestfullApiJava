package com.hamza.fruitsappbackend.validation.validator;

import com.hamza.fruitsappbackend.modulus.cart.dto.CartDTO;
import com.hamza.fruitsappbackend.modulus.product.entity.Product;
import com.hamza.fruitsappbackend.modulus.product.repository.ProductRepository;
import com.hamza.fruitsappbackend.validation.annotation.ValidTotalPrice;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class TotalPriceValidator implements ConstraintValidator<ValidTotalPrice, CartDTO> {

    @Autowired
    private ProductRepository productRepository;

    public boolean isValid(CartDTO cartDTO, ConstraintValidatorContext context) {
        try {
            BigDecimal calculatedTotalPrice = BigDecimal.ZERO;

            for (var item : cartDTO.getCartItems()) {
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

            if (calculatedTotalPrice.compareTo(cartDTO.getTotalPrice()) != 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                                "Provided total price does not match the calculated total price. Calculated: " +
                                        calculatedTotalPrice + ", Provided: " + cartDTO.getTotalPrice())
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
