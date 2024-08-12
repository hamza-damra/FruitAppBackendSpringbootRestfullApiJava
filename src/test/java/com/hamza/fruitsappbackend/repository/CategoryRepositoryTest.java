package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Fruits");
    }

    @Test
    void testSaveCategory() {
        Category savedCategory = categoryRepository.save(category);
        assertNotNull(savedCategory.getId());
        assertEquals("Fruits", savedCategory.getName());
    }

    @Test
    void testFindCategoryById() {
        Category savedCategory = categoryRepository.save(category);
        Optional<Category> retrievedCategory = categoryRepository.findById(savedCategory.getId());
        assertTrue(retrievedCategory.isPresent());
        assertEquals(savedCategory.getId(), retrievedCategory.get().getId());
    }

    @Test
    void testUpdateCategory() {
        Category savedCategory = categoryRepository.save(category);
        savedCategory.setName("Vegetables");
        Category updatedCategory = categoryRepository.save(savedCategory);
        assertEquals("Vegetables", updatedCategory.getName());
    }

    @Test
    void testDeleteCategory() {
        Category savedCategory = categoryRepository.save(category);
        categoryRepository.deleteById(savedCategory.getId());
        Optional<Category> deletedCategory = categoryRepository.findById(savedCategory.getId());
        assertFalse(deletedCategory.isPresent());
    }

    @Test
    void testFindCategoryByName() {
        categoryRepository.save(category);
        Optional<Category> retrievedCategory = categoryRepository.findByName("Fruits");
        assertTrue(retrievedCategory.isPresent());
        assertEquals("Fruits", retrievedCategory.get().getName());
    }
}
