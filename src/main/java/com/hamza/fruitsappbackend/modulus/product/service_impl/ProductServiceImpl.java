package com.hamza.fruitsappbackend.modulus.product.service_impl;

import com.hamza.fruitsappbackend.modulus.cart.entity.Cart;
import com.hamza.fruitsappbackend.modulus.cart.entity.CartItem;
import com.hamza.fruitsappbackend.modulus.cart.exception.CartItemNotFoundException;
import com.hamza.fruitsappbackend.modulus.cart.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.modulus.cart.repository.CartItemRepository;
import com.hamza.fruitsappbackend.modulus.cart.repository.CartRepository;
import com.hamza.fruitsappbackend.modulus.product.dto.ProductDTO;
import com.hamza.fruitsappbackend.modulus.product.entity.Category;
import com.hamza.fruitsappbackend.modulus.product.entity.Product;
import com.hamza.fruitsappbackend.modulus.review.entity.Review;
import com.hamza.fruitsappbackend.modulus.product.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.modulus.product.exception.CategoryNotFoundException;
import com.hamza.fruitsappbackend.modulus.product.repository.CategoryRepository;
import com.hamza.fruitsappbackend.modulus.product.repository.ProductRepository;
import com.hamza.fruitsappbackend.modulus.product.service.ProductService;
import com.hamza.fruitsappbackend.modulus.review.repository.ReviewRepository;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import com.hamza.fruitsappbackend.modulus.product.dto.ProductResponse;
import com.hamza.fruitsappbackend.modulus.wishlist.repository.WishlistRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private final CartRepository cartRepository;
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
        this.cartRepository = cartRepository;
        this.wishlistRepository = wishlistRepository;
        this.cartItemRepository = cartItemRepository;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    @Caching(put = {
            @CachePut(value = "products", key = "#result.id")
    }, evict = {
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    public ProductDTO addProduct(ProductDTO productDTO, String token) {
        authorizationUtils.checkAdminRole(token);
        Product product = convertToEntity(productDTO);
        setCategory(productDTO, product);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct, authorizationUtils.getUserFromToken(token).getId());
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Optional<ProductDTO> getProductById(String token, Long id) {
        Long userId = authorizationUtils.getUserFromToken(token).getId();
        return productRepository.findById(id)
                .map(product -> convertToDto(product, userId));
    }

    @Override
    @Cacheable(value = "productsByCategoryId", key = "#categoryId")
    public List<ProductDTO> getProductsByCategoryId(String token, Long categoryId) {
        Long userId = authorizationUtils.getUserFromToken(token).getId();
        return productRepository.findByCategoryId(categoryId).stream()
                .map(product -> convertToDto(product, userId))
                .collect(Collectors.toList());
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
                .map(contentItem -> convertToDto(contentItem, authorizationUtils.getUserFromToken(token).getId()))
                .collect(Collectors.toList());

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
    @CachePut(value = "products", key = "#productDTO.id")
    public ProductDTO updateProduct(ProductDTO productDTO, String token) {
        authorizationUtils.checkAdminRole(token);
        Product existingProduct = findProductById(productDTO.getId());

        updateProductDetails(productDTO, existingProduct);
        setCategory(productDTO, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct, authorizationUtils.getUserFromToken(token).getId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
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

    private ProductDTO convertToDto(Product product, Long userId) {
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
        productDTO.setIsFavorite(wishlistRepository.existsByUserIdAndProductId(userId, product.getId()));
        productDTO.setIsInCart(cartItemRepository.existsByCartUserIdAndProductId(userId, product.getId()));
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));;
        Optional<CartItem> cartItemOpt = cart.getCartItems().stream().filter(cartItem -> cartItem.getProduct().getId().equals(product.getId())).findFirst();
        productDTO.setQuantityInCart(cartItemOpt.map(CartItem::getQuantity).orElse(0));
        return productDTO;
    }

    private Product convertToEntity(ProductDTO productDTO) {
        return modelMapper.map(productDTO, Product.class);
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
}
