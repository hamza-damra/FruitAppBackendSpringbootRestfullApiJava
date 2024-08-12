package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Object> findByUserId(Long userId);

    Optional<Object> findByProductId(Long productId);
}