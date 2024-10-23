package com.hamza.fruitsappbackend.modules.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDto {

    private BigDecimal totalPrice = BigDecimal.valueOf(0.0);

    private Integer count = 0;

    private List<CartItemDTO> items;
}
