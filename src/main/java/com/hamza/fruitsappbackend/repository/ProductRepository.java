package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String apple);

    Optional<Object> findByCategoryId(Long categoryId);
}