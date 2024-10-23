package com.hamza.fruitsappbackend.modules.product.service;

import com.hamza.fruitsappbackend.modules.product.dto.ProductDTO;
import com.hamza.fruitsappbackend.modules.product.dto.ProductResponse;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    ProductDTO addProduct(ProductDTO productDTO, String token);

    Optional<ProductDTO> getProductById(String token, Long id);

    List<ProductDTO> getProductsByCategoryId(String token, Long categoryId);

    ProductResponse getAllProducts(String token, int pageSize, int pageNumber, String sortBy, String sortDirection);

    ProductDTO updateProduct(ProductDTO productDTO, String token);

    void deleteProductById(Long id, String token);

    void updateProductTotalRating(Long productId);

    void deleteAllProducts(String token);

    List<ProductDTO> searchProducts(String keyword, String token);

    ProductResponse getProductsByPriceRange(String token, double minPrice, double maxPrice, int pageSize, int pageNumber);
}
