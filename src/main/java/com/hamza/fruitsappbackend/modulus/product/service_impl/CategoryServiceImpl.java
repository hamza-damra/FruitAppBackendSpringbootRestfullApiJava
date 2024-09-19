package com.hamza.fruitsappbackend.modulus.product.service_impl;

import com.hamza.fruitsappbackend.modulus.product.dto.CategoryDTO;
import com.hamza.fruitsappbackend.modulus.product.entity.Category;
import com.hamza.fruitsappbackend.modulus.product.exception.CategoryNotFoundException;
import com.hamza.fruitsappbackend.modulus.product.repository.CategoryRepository;
import com.hamza.fruitsappbackend.modulus.product.service.CategoryService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper,
                               AuthorizationUtils authorizationUtils) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    public CategoryDTO saveCategory(CategoryDTO categoryDTO, String token) {
        authorizationUtils.checkAdminRole(token);
        Category category = modelMapper.map(categoryDTO, Category.class);
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    @Cacheable(value = "categories", key = "#id")
    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .orElseThrow(() -> new CategoryNotFoundException("id", id.toString()));
    }

    @Override
    @Cacheable(value = "allCategories")
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, String token) {
        authorizationUtils.checkAdminRole(token);
        Category category = categoryRepository.findById(categoryDTO.getId())
                .orElseThrow(() -> new CategoryNotFoundException("id", categoryDTO.getId().toString()));

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    @Override
    public void deleteCategoryById(Long id, String token) {
        authorizationUtils.checkAdminRole(token);

        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("id", id.toString());
        }
        categoryRepository.deleteById(id);
    }
}
