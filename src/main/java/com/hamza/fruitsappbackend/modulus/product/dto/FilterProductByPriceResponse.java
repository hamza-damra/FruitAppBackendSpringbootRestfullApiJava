package com.hamza.fruitsappbackend.modulus.product.dto;

import com.hamza.fruitsappbackend.modulus.cart.dto.CartItemDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FilterProductByPriceResponse {
    private Integer itemsCount = 0;

    private List<ProductDTO> items;
}
