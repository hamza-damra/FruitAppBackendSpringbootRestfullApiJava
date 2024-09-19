package com.hamza.fruitsappbackend.modulus.cart.service;

import com.hamza.fruitsappbackend.modulus.cart.dto.CartItemDTO;

public interface CartService {
    CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token);
}
