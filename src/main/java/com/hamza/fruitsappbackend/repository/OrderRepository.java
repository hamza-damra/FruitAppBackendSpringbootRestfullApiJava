package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.Order;
import com.hamza.fruitsappbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findByUserId(Long userId);

    void deleteByUser(User user);

    void deleteByIdAndUserId(Long id, Long userId);
}
