package com.hamza.fruitsappbackend.modules.cart.service;

import com.hamza.fruitsappbackend.modules.cart.dto.CartItemDTO;

public interface CartService {
    CartItemDTO addCartItemToCart(CartItemDTO cartItemDTO, String token);
    void completeCart(String token);

    void reopenCart(String token);
}
