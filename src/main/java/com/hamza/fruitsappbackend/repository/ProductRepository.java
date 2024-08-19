package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.dto.ProductDTO;
import com.hamza.fruitsappbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String apple);

    List<Product> findByCategoryId(Long categoryId);
}