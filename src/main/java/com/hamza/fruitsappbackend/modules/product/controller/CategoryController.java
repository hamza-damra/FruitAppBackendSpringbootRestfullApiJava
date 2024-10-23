package com.hamza.fruitsappbackend.modules.product.controller;

import com.hamza.fruitsappbackend.modules.product.dto.CategoryDTO;
import com.hamza.fruitsappbackend.modules.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/add")
    public ResponseEntity<CategoryDTO> createCategory(@RequestHeader("Authorization") String token, @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO savedCategory = categoryService.saveCategory(categoryDTO, token);
        return ResponseEntity.ok(savedCategory);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO categoryDTO = categoryService.getCategoryById(id);
        return ResponseEntity.ok(categoryDTO);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        categoryDTO.setId(id);
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryDTO, token);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        categoryService.deleteCategoryById(id, token);
        return ResponseEntity.noContent().build();
    }
}
