package com.hamza.fruitsappbackend.modulus.product.service;

import com.hamza.fruitsappbackend.modulus.product.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {

    CategoryDTO saveCategory(CategoryDTO categoryDTO, String token);

    CategoryDTO getCategoryById(Long id);

    List<CategoryDTO> getAllCategories();

    CategoryDTO updateCategory(CategoryDTO categoryDTO, String token);

    void deleteCategoryById(Long id, String token);
}