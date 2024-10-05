package com.hamza.fruitsappbackend.modulus.wishlist.service_impl;

import com.hamza.fruitsappbackend.modulus.wishlist.dto.WishlistDTO;
import com.hamza.fruitsappbackend.modulus.product.entity.Product;
import com.hamza.fruitsappbackend.modulus.user.entity.User;
import com.hamza.fruitsappbackend.modulus.wishlist.dto.WishlistResponse;
import com.hamza.fruitsappbackend.modulus.wishlist.entity.Wishlist;
import com.hamza.fruitsappbackend.modulus.product.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.modulus.user.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.modulus.wishlist.exception.WishlistNotFoundException;
import com.hamza.fruitsappbackend.modulus.product.repository.ProductRepository;
import com.hamza.fruitsappbackend.modulus.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modulus.wishlist.repository.WishlistRepository;
import com.hamza.fruitsappbackend.modulus.wishlist.service.WishlistService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistServiceImpl.class);

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AuthorizationUtils authorizationUtils;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public WishlistServiceImpl(WishlistRepository wishlistRepository, UserRepository userRepository,
                               ProductRepository productRepository, AuthorizationUtils authorizationUtils,
                               JwtTokenProvider jwtTokenProvider) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.authorizationUtils = authorizationUtils;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "allWishlists", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    public void addToWishlist(Long productId, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        if (wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            logger.warn("Product with ID {} is already in the wishlist for user ID {}", productId, userId);
            throw new IllegalArgumentException("Product is already in the wishlist");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user_id", userId.toString()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("product_id", productId.toString()));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlistRepository.save(wishlist);
        logger.info("Product with ID {} added to wishlist for user ID {}", productId, userId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "allWishlists", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    public void removeFromWishlist(Long productId, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new WishlistNotFoundException("user_id and product_id", userId + " " + productId.toString()));
        wishlistRepository.delete(wishlist);
        logger.info("Product with ID {} removed from wishlist for user ID {}", productId, userId);
    }

    @Override
    @Cacheable(value = "allWishlists", key = "#token")
    public WishlistResponse getWishlistByUserId(String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
        logger.info("Fetched wishlist for user ID {}", userId);

        return new WishlistResponse(wishlists.size(), wishlists.stream()
                .map(wishlist -> convertToWishlistDTO(wishlist, token))
                .collect(Collectors.toList()));
    }

    @Override
    @Cacheable(value = "allWishlists", key = "#token")
    public WishlistResponse getAllWishlists(String token) {
        authorizationUtils.checkAdminRole(token);
        List<Wishlist> wishlists = wishlistRepository.findAll();
        logger.info("Fetched all wishlists");

        return new WishlistResponse(wishlists.size(), wishlists.stream()
                .map(wishlist -> convertToWishlistDTO(wishlist, token))
                .collect(Collectors.toList()));
    }

    private WishlistDTO convertToWishlistDTO(Wishlist wishlist, String token) {
        WishlistDTO wishlistDTO = new WishlistDTO();
        wishlistDTO.setId(wishlist.getId());
        wishlistDTO.setProductId(wishlist.getProduct().getId());
        wishlistDTO.setCategoryId(wishlist.getProduct().getCategory().getId());
        wishlistDTO.setName(wishlist.getProduct().getName());
        wishlistDTO.setPrice(wishlist.getProduct().getPrice());
        wishlistDTO.setQuantityInCart(wishlist.getProduct().getQuantityInCart());
        wishlistDTO.setTotalRating(wishlist.getProduct().getTotalRating());
        wishlistDTO.setAddedAt(wishlist.getProduct().getCreatedAt());
        wishlistDTO.setDescription(wishlist.getProduct().getDescription());
        wishlistDTO.setImageUrl(wishlist.getProduct().getImageUrl());
        wishlistDTO.setStockQuantity(wishlist.getProduct().getStockQuantity());
        wishlistDTO.setProductWeight(wishlist.getProduct().getProductWeight());
        wishlistDTO.setQuantityInCart(wishlist.getProduct().getQuantityInCart() == null ? 0 : wishlist.getProduct().getQuantityInCart());
        wishlistDTO.setCaloriesPer100Grams(wishlist.getProduct().getCaloriesPer100Grams());
        wishlistDTO.setExpirationDate(wishlist.getProduct().getExpirationDate());
        wishlistDTO.setCounterOneStars(wishlist.getProduct().getCounterOneStars());
        wishlistDTO.setCounterTwoStars(wishlist.getProduct().getCounterTwoStars());
        wishlistDTO.setCounterThreeStars(wishlist.getProduct().getCounterThreeStars());
        wishlistDTO.setCounterFourStars(wishlist.getProduct().getCounterFourStars());
        wishlistDTO.setCounterFiveStars(wishlist.getProduct().getCounterFiveStars());
        wishlistDTO.setCreatedAt(wishlist.getCreatedAt());

        User user = authorizationUtils.getUserFromToken(token);

        boolean isFavorite = user.getWishlistItems().stream()
                .anyMatch(wishlistItem -> wishlistItem.getProduct().getId().equals(wishlist.getProduct().getId()));
        wishlistDTO.setFavorite(isFavorite);


        boolean isInCart = user.getCart().getCartItems().stream()
                .anyMatch(cartItem -> cartItem.getProduct().getId().equals(wishlist.getProduct().getId()));
        wishlistDTO.setInCart(isInCart);

        return wishlistDTO;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "allWishlists", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    public void removeAllFromWishlist(String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        wishlistRepository.deleteAllByUserId(userId);
        logger.info("Removed all products from wishlist for user ID {}", userId);
    }

    private Long getUserIdFromToken(String token) {
        return Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));
    }
}
