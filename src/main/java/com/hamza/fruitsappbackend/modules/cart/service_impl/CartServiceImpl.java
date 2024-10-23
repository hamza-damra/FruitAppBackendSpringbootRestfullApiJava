package com.hamza.fruitsappbackend.modules.cart.service_impl;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.exception.global.BadRequestException;
import com.hamza.fruitsappbackend.modules.cart.dto.CartItemDTO;
import com.hamza.fruitsappbackend.modules.cart.entity.Cart;
import com.hamza.fruitsappbackend.modules.cart.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.modules.user.entity.User;
import com.hamza.fruitsappbackend.modules.user.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.modules.cart.repository.CartRepository;
import com.hamza.fruitsappbackend.modules.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modules.cart.service.CartItemService;
import com.hamza.fruitsappbackend.modules.cart.service.CartService;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

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

        // Fetch the active cart for the user
        List<Cart> activeCarts = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE);

        if (activeCarts.size() > 1) {
            // Throw an error if more than one active cart exists, indicating a data inconsistency
            throw new IllegalStateException("Multiple active carts found for user. Data inconsistency detected.");
        }

        Cart cart;
        if (activeCarts.isEmpty()) {
            // Create a new cart if no active cart is found
            cart = new Cart();
            cart.setUser(user);
            cart.setTotalPrice(BigDecimal.ZERO);
            cart.setTotalQuantity(0);
            cart.setStatus(CartStatus.ACTIVE);
            cartRepository.save(cart); // Save the new cart
        } else {
            // Use the existing active cart
            cart = activeCarts.get(0);
        }

        // Add cart item to the active cart
        return cartItemService.addCartItemToCart(cart.getId(), cartItemDTO, token);
    }

    @Override
    @Transactional
    public void completeCart(String token) {
        Long userId = getUserIdFromToken(token);

        // Fetch all active carts for the user
        List<Cart> activeCarts = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE);

        if (activeCarts.size() > 1) {
            throw new IllegalStateException("Multiple active carts found for user. Data inconsistency detected.");
        }

        if (activeCarts.isEmpty()) {
            throw new CartNotFoundException("userId", userId.toString());
        }

        // Get the active cart
        Cart cart = activeCarts.get(0);

        // Check if the cart is already completed
        if (cart.getStatus() == CartStatus.COMPLETED) {
            throw new BadRequestException("Cart is already completed");
        }

        // Mark the cart as completed
        cart.setStatus(CartStatus.COMPLETED);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void reopenCart(String token) {
        Long userId = getUserIdFromToken(token);

        // Fetch all completed carts for the user
        List<Cart> completedCarts = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.COMPLETED);

        if (completedCarts.size() > 1) {
            throw new IllegalStateException("Multiple completed carts found for user. Data inconsistency detected.");
        }

        if (completedCarts.isEmpty()) {
            throw new CartNotFoundException("userId", userId.toString());
        }

        // Get the completed cart
        Cart cart = completedCarts.get(0);

        // Reopen the cart and mark it as active
        cart.setStatus(CartStatus.ACTIVE);
        cartRepository.save(cart);
    }

    private Long getUserIdFromToken(String token) {
        return Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));
    }
}
