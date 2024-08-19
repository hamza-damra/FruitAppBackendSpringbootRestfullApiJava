package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.CategoryDTO;
import com.hamza.fruitsappbackend.entity.Category;
import com.hamza.fruitsappbackend.exception.CategoryNotFoundException;
import com.hamza.fruitsappbackend.repository.CategoryRepository;
import com.hamza.fruitsappbackend.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .or(() -> {
                    throw new CategoryNotFoundException("id", id.toString());
                });
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(categoryDTO.getId())
                .orElseThrow(() -> new CategoryNotFoundException("id", categoryDTO.getId().toString()));

        modelMapper.map(categoryDTO, category); // Update existing entity
        Category updatedCategory = categoryRepository.save(category);
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    @Override
    public void deleteCategoryById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("id", id.toString());
        }
        categoryRepository.deleteById(id);
    }
}
