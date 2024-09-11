package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.CartItem;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductId(Long id, @NotNull(message = "Product ID cannot be null") Long productId);

    boolean existsByCartUserIdAndProductId(Long userId, Long id);
}