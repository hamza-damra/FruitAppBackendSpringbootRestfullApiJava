package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.CartItemDTO;

import java.util.List;
import java.util.Optional;

public interface CartItemService {

    CartItemDTO saveCartItem(Long cartId, CartItemDTO cartItemDTO, String token);

    Optional<CartItemDTO> getCartItemById(Long id, String token);

    List<CartItemDTO> getCartItemsByCartId(Long cartId, String token);

    List<CartItemDTO> getAllCartItems(String token);

    CartItemDTO updateCartItem(Long cartId, CartItemDTO cartItemDTO, String token);

    void deleteCartItemById(Long cartId, Long id, String token);
}
