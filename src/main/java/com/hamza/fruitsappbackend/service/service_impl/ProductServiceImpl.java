package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.ProductDTO;
import com.hamza.fruitsappbackend.entity.Category;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.exception.CategoryNotFoundException;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.repository.CategoryRepository;
import com.hamza.fruitsappbackend.service.ProductService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
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
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
                              ModelMapper modelMapper, AuthorizationUtils authorizationUtils) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    public ProductDTO saveProduct(ProductDTO productDTO, String token) {
        authorizationUtils.checkAdminRole(token);
        Product product = convertToEntity(productDTO);
        setCategory(productDTO, product);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    @Override
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, String token) {
        authorizationUtils.checkAdminRole(token);
        Product existingProduct = findProductById(productDTO.getId());
        updateProductDetails(productDTO, existingProduct);
        setCategory(productDTO, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    @Override
    public void deleteProductById(Long id, String token) {
        authorizationUtils.checkAdminRole(token);
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    private void setCategory(ProductDTO productDTO, Product product) {
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("id", productDTO.getCategoryId().toString()));
            product.setCategory(category);
        } else {
            product.setCategory(null); // Allow setting category to null
        }
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("id", id.toString()));
    }

    private void updateProductDetails(ProductDTO productDTO, Product product) {
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());
        product.setStockQuantity(productDTO.getStockQuantity());
    }

    private ProductDTO convertToDto(Product product) {
        return modelMapper.map(product, ProductDTO.class);
    }

    private Product convertToEntity(ProductDTO productDTO) {
        return modelMapper.map(productDTO, Product.class);
    }
}
