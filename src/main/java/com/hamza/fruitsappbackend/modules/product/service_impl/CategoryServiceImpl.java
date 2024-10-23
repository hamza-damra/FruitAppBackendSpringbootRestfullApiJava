package com.hamza.fruitsappbackend.modules.product.service_impl;

import com.hamza.fruitsappbackend.modules.product.dto.CategoryDTO;
import com.hamza.fruitsappbackend.modules.product.entity.Category;
import com.hamza.fruitsappbackend.modules.product.exception.CategoryNotFoundException;
import com.hamza.fruitsappbackend.modules.product.repository.CategoryRepository;
import com.hamza.fruitsappbackend.modules.product.repository.ProductRepository;
import com.hamza.fruitsappbackend.modules.product.service.CategoryService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository, ModelMapper modelMapper,
                               AuthorizationUtils authorizationUtils) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
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
    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .orElseThrow(() -> new CategoryNotFoundException("id", id.toString()));
    }

    @Override
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

        Optional<Category> category = categoryRepository.findById(id);

        if (category.isEmpty()) {
            throw new CategoryNotFoundException("id", id.toString());
        }
        if(!category.get().getProducts().isEmpty()) {
            productRepository.deleteAllByCategoryId(id);
        }

        categoryRepository.deleteById(id);
    }
}
