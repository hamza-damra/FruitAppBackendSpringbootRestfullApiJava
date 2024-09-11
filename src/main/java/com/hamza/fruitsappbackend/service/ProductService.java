package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.ProductDTO;
import com.hamza.fruitsappbackend.payload.ProductResponse;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    ProductDTO saveProduct(ProductDTO productDTO, String token);

    Optional<ProductDTO> getProductById(String token, Long id);

    List<ProductDTO> getProductsByCategoryId(String token, Long categoryId);

    ProductResponse getAllProducts(String token, int pageSize, int pageNumber, String sortBy, String sortDirection);

    ProductDTO updateProduct(ProductDTO productDTO, String token);

    void deleteProductById(Long id, String token);

    void updateProductTotalRating(Long productId);
}
