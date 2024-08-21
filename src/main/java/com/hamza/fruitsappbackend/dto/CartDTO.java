package com.hamza.fruitsappbackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hamza.fruitsappbackend.validators.annotation.UniqueUserCart;
import com.hamza.fruitsappbackend.validators.markers.OnCreate;
import com.hamza.fruitsappbackend.validators.markers.OnUpdate;
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

    @NotNull(message = "User ID cannot be null", groups = {OnCreate.class, OnUpdate.class})
    @UniqueUserCart(message = "User can have only one cart", groups = OnCreate.class)
    private Long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @NotNull(message = "Cart items cannot be null", groups = {OnCreate.class, OnUpdate.class})
    private List<CartItemDTO> cartItems;
}
