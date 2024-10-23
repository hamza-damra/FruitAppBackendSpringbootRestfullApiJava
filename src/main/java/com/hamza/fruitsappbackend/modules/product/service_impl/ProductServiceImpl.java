package com.hamza.fruitsappbackend.modules.product.service_impl;

import com.hamza.fruitsappbackend.modules.cart.repository.CartItemRepository;
import com.hamza.fruitsappbackend.modules.cart.repository.CartRepository;
import com.hamza.fruitsappbackend.modules.product.dto.ProductDTO;
import com.hamza.fruitsappbackend.modules.product.entity.Category;
import com.hamza.fruitsappbackend.modules.product.entity.Product;
import com.hamza.fruitsappbackend.modules.review.entity.Review;
import com.hamza.fruitsappbackend.modules.product.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.modules.product.exception.CategoryNotFoundException;
import com.hamza.fruitsappbackend.modules.product.repository.CategoryRepository;
import com.hamza.fruitsappbackend.modules.product.repository.ProductRepository;
import com.hamza.fruitsappbackend.modules.product.service.ProductService;
import com.hamza.fruitsappbackend.modules.review.repository.ReviewRepository;
import com.hamza.fruitsappbackend.modules.user.entity.User;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import com.hamza.fruitsappbackend.modules.product.dto.ProductResponse;
import com.hamza.fruitsappbackend.modules.wishlist.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ModelMapper modelMapper;
    private final WishlistRepository wishlistRepository;
    private final CartItemRepository cartItemRepository;
    private final AuthorizationUtils authorizationUtils;
    private static final Logger logger = LogManager.getLogger(ProductServiceImpl.class);

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
                              ReviewRepository reviewRepository, ModelMapper modelMapper, CartRepository cartRepository, WishlistRepository wishlistRepository,
                              CartItemRepository cartItemRepository, AuthorizationUtils authorizationUtils) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.modelMapper = modelMapper;
        this.wishlistRepository = wishlistRepository;
        this.cartItemRepository = cartItemRepository;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    @CacheEvict(value = "allProducts", allEntries = true)
    public ProductDTO addProduct(ProductDTO productDTO, String token) {
        authorizationUtils.checkAdminRole(token);
        Product product = convertToEntity(productDTO);
        setCategory(productDTO, product);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct, token);
    }

    @Override
    public Optional<ProductDTO> getProductById(String token, Long id) {
        return productRepository.findById(id)
                .map(p -> convertToDto(p, token));
    }

    @Override
    public List<ProductDTO> getProductsByCategoryId(String token, Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(p -> convertToDto(p, token)).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "allProducts", key = "T(String).valueOf(#pageSize) + '-' + T(String).valueOf(#pageNumber) + '-' + #sortBy + '-' + #sortDirection")
    public ProductResponse getAllProducts(String token, int pageSize, int pageNumber, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        long startTime = System.currentTimeMillis();
        Page<Product> productPage = productRepository.findAll(pageable);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        logger.info("Product retrieval query executed in: {} ms", executionTime);
        List<ProductDTO> content = productPage.getContent().stream()
                .map(contentItem -> convertToDto(contentItem, token)).toList();

        return new ProductResponse(
                productPage.getSize(),
                productPage.getNumber(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                content
        );
    }

    @Override
    @CacheEvict(value = "allProducts", allEntries = true)
    public ProductDTO updateProduct(ProductDTO productDTO, String token) {
        authorizationUtils.checkAdminRole(token);
        Product existingProduct = findProductById(productDTO.getId());

        updateProductDetails(productDTO, existingProduct);
        setCategory(productDTO, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct, token);
    }

    @Override
    @Transactional
    @CacheEvict(value = "allProducts", allEntries = true)
    public void deleteProductById(Long id, String token) {
        authorizationUtils.checkAdminRole(token);
        Product product = findProductById(id);
        if(!product.getCartItems().isEmpty()) {
            cartItemRepository.deleteAllByProductId(product.getId());
        }
        if(!product.getReviews().isEmpty()) {
            reviewRepository.deleteAllByProductId(product.getId());
        }
        if (!product.getWishlists().isEmpty()) {
            wishlistRepository.deleteAllByProductId(product.getId());
        }
        productRepository.delete(product);
    }

    private void setCategory(ProductDTO productDTO, Product product) {
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("id", productDTO.getCategoryId().toString()));
            product.setCategory(category);
        }
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("id", id.toString()));
    }

    private void updateProductDetails(ProductDTO productDTO, Product product) {
        product.setName(Optional.ofNullable(productDTO.getName()).orElse(product.getName()));
        product.setDescription(Optional.ofNullable(productDTO.getDescription()).orElse(product.getDescription()));
        product.setPrice(Optional.ofNullable(productDTO.getPrice()).orElse(product.getPrice()));
        product.setStockQuantity(Optional.ofNullable(productDTO.getStockQuantity()).orElse(product.getStockQuantity()));
        product.setImageUrl(Optional.ofNullable(productDTO.getImageUrl()).orElse(product.getImageUrl()));
        product.setProductWeight(Optional.ofNullable(productDTO.getProductWeight()).orElse(product.getProductWeight()));
        product.setCaloriesPer100Grams(Optional.ofNullable(productDTO.getCaloriesPer100Grams()).orElse(product.getCaloriesPer100Grams()));
        product.setExpirationDate(Optional.ofNullable(productDTO.getExpirationDate()).orElse(product.getExpirationDate()));
    }

    private ProductDTO convertToDto(Product product, String token) {
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        User user = authorizationUtils.getUserFromToken(token);

        // Determine if the product is in the user's wishlist
        boolean isFavorite = user.getWishlistItems().stream()
                .anyMatch(wishlistItem -> wishlistItem.getProduct().getId().equals(product.getId()));
        productDTO.setIsFavorite(isFavorite);

        // Determine if the product is in the user's cart
        boolean isInCart = user.getCarts().stream()
                .flatMap(cart -> cart.getCartItems().stream())
                .anyMatch(cartItem -> cartItem.getProduct().getId().equals(product.getId()));
        productDTO.setIsInCart(isInCart);

        productDTO.setAddedAt(product.getCreatedAt());
        return productDTO;
    }

    private Product convertToEntity(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setImageUrl(productDTO.getImageUrl());
        product.setProductWeight(productDTO.getProductWeight());
        product.setCaloriesPer100Grams(productDTO.getCaloriesPer100Grams());
        product.setExpirationDate(productDTO.getExpirationDate());
        product.setTotalRating(0.0);
        product.setQuantityInCart(0);
        product.setOrderCount(0L);

        return product;
    }

    public void updateProductTotalRating(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);

        double totalRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        Product product = findProductById(productId);
        product.setTotalRating(totalRating);
        product.setCounterFiveStars((int) reviews.stream().filter(r -> r.getRating() == 5).count());
        product.setCounterFourStars((int) reviews.stream().filter(r -> r.getRating() == 4).count());
        product.setCounterThreeStars((int) reviews.stream().filter(r -> r.getRating() == 3).count());
        product.setCounterTwoStars((int) reviews.stream().filter(r -> r.getRating() == 2).count());
        product.setCounterOneStars((int) reviews.stream().filter(r -> r.getRating() == 1).count());
        productRepository.save(product);
    }

    @Override
    @CacheEvict(value = "allProducts", allEntries = true)
    public void deleteAllProducts(String token) {
        authorizationUtils.checkAdminRole(token);
        productRepository.deleteAll();
    }

    @Override
    public List<ProductDTO> searchProducts(String keyword, String token) {
        List<Product> products = productRepository.searchByName(keyword);

        if (products.isEmpty()) {
            products = productRepository.searchByDescription(keyword);
        }

        return products.stream()
                .map(product -> convertToDto(product, token))
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductsByPriceRange(String token, double minPrice, double maxPrice, int pageSize, int pageNumber) {
        // Check user authorization
        authorizationUtils.checkUserOrAdminRole(token, authorizationUtils.getUserFromToken(token).getId());

        // Validate pageSize and pageNumber to prevent out-of-bounds access
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number must be 0 or greater");
        }

        // Create a Pageable object
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Fetch products by price range with pagination
        Page<Product> productPage = productRepository.findByPriceRange(minPrice, maxPrice, pageable);

        // Convert the retrieved products to DTOs
        List<ProductDTO> content = productPage.getContent().stream()
                .map(product -> convertToDto(product, token))
                .collect(Collectors.toList());

        // Construct and return the ProductResponse with pagination details
        return new ProductResponse(
                productPage.getSize(),          // Current page size
                productPage.getNumber(),        // Current page number
                productPage.getTotalElements(), // Total number of products
                productPage.getTotalPages(),    // Total number of pages
                productPage.isLast(),           // Is this the last page?
                content                         // List of products in DTO form
        );
    }

}
