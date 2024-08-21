package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.CategoryDTO;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    CategoryDTO saveCategory(CategoryDTO categoryDTO, String token);

    Optional<CategoryDTO> getCategoryById(Long id);

    List<CategoryDTO> getAllCategories();

    CategoryDTO updateCategory(CategoryDTO categoryDTO, String token);

    void deleteCategoryById(Long id, String token);
}
