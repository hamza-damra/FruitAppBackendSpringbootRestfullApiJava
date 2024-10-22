package com.hamza.fruitsappbackend.modulus.cart.repository;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.modulus.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Use this method to fetch carts filtered by both userId and status
    List<Cart> findAllByUserIdAndStatus(Long userId, CartStatus cartStatus);

    // Remove this method to avoid returning unfiltered results
    // Optional<Cart> findByUserId(Long userId);
}

