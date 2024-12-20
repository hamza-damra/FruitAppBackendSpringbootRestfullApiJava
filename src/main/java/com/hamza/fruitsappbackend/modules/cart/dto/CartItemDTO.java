package com.hamza.fruitsappbackend.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Long id;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private int stockQuantity;

    private String productName;

    private String productImageUrl;

    private Double price;
}
