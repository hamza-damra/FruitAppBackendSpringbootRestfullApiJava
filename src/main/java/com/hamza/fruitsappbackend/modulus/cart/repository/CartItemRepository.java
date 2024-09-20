package com.hamza.fruitsappbackend.modulus.cart.repository;

import com.hamza.fruitsappbackend.modulus.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    boolean existsByCartUserIdAndProductId(Long userId, Long id);

    void deleteAllByCartId(Long id);

    boolean existsByCartIdAndProductId(Long cartId, Long productId);

    Optional<CartItem> findByProductId(Long productId);
}