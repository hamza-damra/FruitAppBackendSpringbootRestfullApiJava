package com.hamza.fruitsappbackend.modulus.cart.service_impl;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.exception.global.BadRequestException;
import com.hamza.fruitsappbackend.modulus.cart.dto.CartItemDTO;
import com.hamza.fruitsappbackend.modulus.cart.entity.Cart;
import com.hamza.fruitsappbackend.modulus.cart.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.modulus.user.entity.User;
import com.hamza.fruitsappbackend.modulus.user.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.modulus.cart.repository.CartRepository;
import com.hamza.fruitsappbackend.modulus.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modulus.cart.service.CartItemService;
import com.hamza.fruitsappbackend.modulus.cart.service.CartService;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
    public CartItemDTO addCartItemToCart(CartItemDTO cartItemDTO, String token) {
        Long userId = getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("id", userId.toString()));

        Cart cart = user.getCart();

        return cartItemService.addCartItemToCart(cart.getId(), cartItemDTO, token);
    }

    @Override
    public void completeCart(String token) {

        Long userId = getUserIdFromToken(token);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        if (cart.getStatus() == CartStatus.COMPLETED) {
            throw new BadRequestException("Cart is already completed");
        }

        cart.setStatus(CartStatus.COMPLETED);
        cartRepository.save(cart);
    }

    @Override
    public void reopenCart(String token) {
        Long userId = getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        if (cart.getStatus() != CartStatus.COMPLETED) {
            throw new BadRequestException("Only completed carts can be reopened");
        }

        cart.setStatus(CartStatus.ACTIVE);
        cartRepository.save(cart);
    }

    private Long getUserIdFromToken(String token) {
        return Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));
    }
}
