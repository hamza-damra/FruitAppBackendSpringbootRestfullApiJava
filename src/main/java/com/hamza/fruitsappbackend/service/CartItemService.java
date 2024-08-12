package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.CartItemDTO;

import java.util.List;
import java.util.Optional;

public interface CartItemService {

    CartItemDTO saveCartItem(CartItemDTO cartItemDTO);

    Optional<CartItemDTO> getCartItemById(Long id);

    List<CartItemDTO> getCartItemsByCartId(Long cartId);

    List<CartItemDTO> getAllCartItems();

    CartItemDTO updateCartItem(CartItemDTO cartItemDTO);

    void deleteCartItemById(Long id);
}
