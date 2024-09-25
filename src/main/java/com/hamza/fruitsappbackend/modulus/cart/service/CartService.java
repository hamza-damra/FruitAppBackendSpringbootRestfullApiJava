package com.hamza.fruitsappbackend.modulus.cart.service;

import com.hamza.fruitsappbackend.modulus.cart.dto.CartItemDTO;

public interface CartService {
    CartItemDTO addCartItemToCart(CartItemDTO cartItemDTO, String token);
    void completeCart(String token);

    void reopenCart(String token);
}
