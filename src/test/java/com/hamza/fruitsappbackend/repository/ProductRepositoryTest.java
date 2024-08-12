package com.hamza.fruitsappbackend.repository;


import com.hamza.fruitsappbackend.entity.Category;
import com.hamza.fruitsappbackend.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Fruits");
        categoryRepository.save(category);

        product = new Product();
        product.setName("Apple");
        product.setDescription("Fresh Red Apple");
        product.setPrice(1.5);
        product.setStockQuantity(100);
        product.setCategory(category);
    }

    @Test
    void testSaveProduct() {
        Product savedProduct = productRepository.save(product);
        assertNotNull(savedProduct.getId());
        assertEquals("Apple", savedProduct.getName());
    }

    @Test
    void testFindProductById() {
        Product savedProduct = productRepository.save(product);
        Optional<Product> retrievedProduct = productRepository.findById(savedProduct.getId());
        assertTrue(retrievedProduct.isPresent());
        assertEquals(savedProduct.getId(), retrievedProduct.get().getId());
    }

    @Test
    void testUpdateProduct() {
        Product savedProduct = productRepository.save(product);
        savedProduct.setPrice(2.0);
        Product updatedProduct = productRepository.save(savedProduct);
        assertEquals(2.0, updatedProduct.getPrice());
    }

    @Test
    void testDeleteProduct() {
        Product savedProduct = productRepository.save(product);
        productRepository.deleteById(savedProduct.getId());
        Optional<Product> deletedProduct = productRepository.findById(savedProduct.getId());
        assertFalse(deletedProduct.isPresent());
    }

    @Test
    void testFindProductByName() {
        productRepository.save(product);
        Optional<Product> retrievedProduct = productRepository.findByName("Apple");
        assertTrue(retrievedProduct.isPresent());
        assertEquals("Apple", retrievedProduct.get().getName());
    }
}
