package com.hamza.fruitsappbackend.modules.product.repository;

import com.hamza.fruitsappbackend.modules.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String fruits);

    Optional<Category> findCategoryById(Long categoryId);
}
