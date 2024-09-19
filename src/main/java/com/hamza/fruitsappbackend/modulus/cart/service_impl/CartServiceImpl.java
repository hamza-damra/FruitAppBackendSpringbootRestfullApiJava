package com.hamza.fruitsappbackend.modulus.cart.service_impl;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.modulus.cart.dto.CartItemDTO;
import com.hamza.fruitsappbackend.modulus.cart.entity.Cart;
import com.hamza.fruitsappbackend.modulus.user.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.modulus.cart.repository.CartRepository;
import com.hamza.fruitsappbackend.modulus.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modulus.cart.service.CartItemService;
import com.hamza.fruitsappbackend.modulus.cart.service.CartService;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartItemService cartItemService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository,
                           CartItemService cartItemService,
                            JwtTokenProvider jwtTokenProvider) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.cartItemService = cartItemService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token) {
        Long userId = getUserIdFromToken(token);

        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createNewCart(userId));

        return cartItemService.addCartItemToCart(cart.getId(), cartItemDTO, token);
    }

    private Long getUserIdFromToken(String token) {
        return Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));
    }

    private Cart createNewCart(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                })
                .orElseThrow(() -> new UserNotFoundException("id", userId.toString()));
    }

}
