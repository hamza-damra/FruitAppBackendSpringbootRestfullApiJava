package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Object> findByUserId(Long userId);
}