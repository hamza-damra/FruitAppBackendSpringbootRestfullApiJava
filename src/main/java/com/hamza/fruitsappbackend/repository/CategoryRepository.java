package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String fruits);

    Optional<Category> findCategoryById(Long categoryId);
}
