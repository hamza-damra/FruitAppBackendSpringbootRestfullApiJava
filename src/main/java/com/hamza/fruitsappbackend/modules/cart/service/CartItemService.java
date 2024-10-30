package com.hamza.fruitsappbackend.modules.cart.service;

import com.hamza.fruitsappbackend.modules.cart.dto.CartItemDTO;
import com.hamza.fruitsappbackend.modules.cart.dto.CartItemResponseDto;
import com.hamza.fruitsappbackend.modules.cart.dto.CartResponseDto;

public interface CartItemService {

    CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token);

    CartResponseDto getCartItemsByUser(String token);

    CartItemDTO getCartItemByProductId(Long productId, String token);


    CartItemDTO updateCartItem(Long cartId, CartItemDTO cartItemDTO, String token);

    CartItemResponseDto deleteCartItemByProductId(Long productId, String token);

    void deleteAllCartItemsByUser(String token);

    CartItemDTO increaseCartItemQuantity(Long productId, String token);
    CartItemDTO decreaseCartItemQuantity(Long productId, String token);
}
