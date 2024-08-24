package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.ProductDTO;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    ProductDTO saveProduct(ProductDTO productDTO, String token);

    Optional<ProductDTO> getProductById(Long id);

    List<ProductDTO> getProductsByCategoryId(Long categoryId);

    List<ProductDTO> getAllProducts();

    ProductDTO updateProduct(ProductDTO productDTO, String token);

    void deleteProductById(Long id, String token);

    void updateProductTotalRating(Long productId);
}
