package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.CartDTO;
import com.hamza.fruitsappbackend.dto.CartItemDTO;

import java.util.List;
import java.util.Optional;

public interface CartService {

    CartDTO saveCart(CartDTO cartDTO, String token);

    Optional<CartDTO> getCartById(Long id, String token);


    List<CartDTO> getAllCarts(String token);

    CartDTO updateCart(CartDTO cartDTO, String token);

    void deleteCartById(Long id, String token);

    CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token);

    void removeCartItemFromCart(Long cartId, Long cartItemId, String token);

    CartDTO getCartByUserId(String token);

    void deleteCartByUserId(String token);
    CartDTO completeCart(Long cartId, String token);

}
