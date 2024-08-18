package com.hamza.fruitsappbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private Long id;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NotNull(message = "Cart items cannot be null")
    private List<CartItemDTO> cartItems;
}
