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
    public ResponseEntity<CategoryDTO> createCategory(@RequestHeader("Authorization") String token, @RequestBody CategoryDTO categoryDTO) {
        String jwtToken = token.replace("Bearer ", "");
        CategoryDTO savedCategory = categoryService.saveCategory(categoryDTO, jwtToken);
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
    public ResponseEntity<CategoryDTO> updateCategory(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        String jwtToken = token.replace("Bearer ", "");
        categoryDTO.setId(id);
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryDTO, jwtToken);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        categoryService.deleteCategoryById(id, jwtToken);
        return ResponseEntity.noContent().build();
    }
}
