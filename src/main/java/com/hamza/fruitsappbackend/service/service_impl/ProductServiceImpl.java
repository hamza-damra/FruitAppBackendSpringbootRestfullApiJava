package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.ProductDTO;
import com.hamza.fruitsappbackend.entity.Category;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.exception.CategoryNotFoundException;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.repository.CategoryRepository;
import com.hamza.fruitsappbackend.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductDTO saveProduct(ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);

        // If category ID is present, fetch the category and set it
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("id", productDTO.getCategoryId().toString()));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(product -> modelMapper.map(product, ProductDTO.class));
    }

    @Override
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO) {
        // Fetch the existing product from the database
        Product existingProduct = productRepository.findById(productDTO.getId())
                .orElseThrow(() -> new ProductNotFoundException("id", productDTO.getId().toString()));

        // Update the fields of the existing product
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setImageUrl(productDTO.getImageUrl());
        existingProduct.setStockQuantity(productDTO.getStockQuantity());

        // Refactored category update
        updateCategory(productDTO, existingProduct);

        // Save the updated product
        Product updatedProduct = productRepository.save(existingProduct);

        // Map the updated product back to the DTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    private void updateCategory(ProductDTO productDTO, Product existingProduct) {
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("id", productDTO.getCategoryId().toString()));
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null); // Handle the case where category might be set to null
        }
    }

    @Override
    public void deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("id", id.toString());
        }
        productRepository.deleteById(id);
    }
}
