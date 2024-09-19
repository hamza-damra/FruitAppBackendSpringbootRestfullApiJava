package com.hamza.fruitsappbackend.modulus.cart.service;

import com.hamza.fruitsappbackend.modulus.cart.dto.CartItemDTO;

import java.util.List;

public interface CartItemService {

    CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token);

    List<CartItemDTO> getCartItemsByUser(String token);

    CartItemDTO getCartItemByProductId(Long productId, String token);


    CartItemDTO updateCartItem(Long cartId, CartItemDTO cartItemDTO, String token);

    void deleteCartItemByProductId(Long productId, String token);
}
