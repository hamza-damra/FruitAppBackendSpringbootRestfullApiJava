package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.CategoryDTO;
import com.hamza.fruitsappbackend.entity.Category;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.exception.CategoryNotFoundException;
import com.hamza.fruitsappbackend.repository.CategoryRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import com.hamza.fruitsappbackend.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, UserRepository userRepository,
                               ModelMapper modelMapper, JwtTokenProvider jwtTokenProvider) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private void checkAdminRole(String token) {
        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }
    }

    @Override
    public CategoryDTO saveCategory(CategoryDTO categoryDTO, String token) {
        checkAdminRole(token);

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
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, String token) {
        checkAdminRole(token);

        Category category = categoryRepository.findById(categoryDTO.getId())
                .orElseThrow(() -> new CategoryNotFoundException("id", categoryDTO.getId().toString()));

        modelMapper.map(categoryDTO, category); // Update existing entity
        Category updatedCategory = categoryRepository.save(category);
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    @Override
    public void deleteCategoryById(Long id, String token) {
        checkAdminRole(token);

        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("id", id.toString());
        }
        categoryRepository.deleteById(id);
    }
}
