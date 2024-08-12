package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Optional<Object> findByOrderId(Long orderId);
}
