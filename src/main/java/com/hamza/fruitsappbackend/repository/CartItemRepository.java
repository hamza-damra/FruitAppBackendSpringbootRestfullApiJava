package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<Object> findByCartId(Long cartId);
}