package com.hamza.fruitsappbackend.modules.cart.repository;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.modules.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Use this method to fetch carts filtered by both userId and status
    List<Cart> findAllByUserIdAndStatus(Long userId, CartStatus cartStatus);

    // Remove this method to avoid returning unfiltered results
    // Optional<Cart> findByUserId(Long userId);
}

