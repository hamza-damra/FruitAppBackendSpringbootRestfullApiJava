package com.hamza.fruitsappbackend.dto;

import jakarta.validation.Valid;
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

    @NotNull(message = "Cart Item ID cannot be null")
    private Long id;

    @NotNull(message = "Product cannot be null")
    @Valid
    private ProductDTO product;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Min(value = 0, message = "Price cannot be negative")
    private double price;

    @NotNull(message = "Cart ID cannot be null")
    private Long cartId;
}
