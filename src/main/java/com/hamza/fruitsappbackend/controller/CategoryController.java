package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.CategoryDTO;
import com.hamza.fruitsappbackend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO savedCategory = categoryService.saveCategory(categoryDTO);
        return ResponseEntity.ok(savedCategory);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        Optional<CategoryDTO> categoryDTO = categoryService.getCategoryById(id);
        return categoryDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        categoryDTO.setId(id);
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }
}
