package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.CartDTO;

import java.util.List;
import java.util.Optional;

public interface CartService {

    CartDTO saveCart(CartDTO cartDTO);

    Optional<CartDTO> getCartById(Long id);

    List<CartDTO> getCartsByUserId(Long userId);

    List<CartDTO> getAllCarts();

    CartDTO updateCart(CartDTO cartDTO);

    void deleteCartById(Long id);
}
