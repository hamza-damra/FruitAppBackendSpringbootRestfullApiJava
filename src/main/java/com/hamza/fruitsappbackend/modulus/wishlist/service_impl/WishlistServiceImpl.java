package com.hamza.fruitsappbackend.modulus.wishlist.service_impl;

import com.hamza.fruitsappbackend.modulus.wishlist.dto.WishlistDTO;
import com.hamza.fruitsappbackend.modulus.product.entity.Product;
import com.hamza.fruitsappbackend.modulus.user.entity.User;
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
    @CacheEvict(value = "allProducts", allEntries = true)
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
    @CacheEvict(value = "allProducts", allEntries = true)
    public void removeFromWishlist(Long productId, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new WishlistNotFoundException("user_id and product_id", userId.toString() + " " + productId.toString()));
        wishlistRepository.delete(wishlist);
        logger.info("Product with ID {} removed from wishlist for user ID {}", productId, userId);
    }

    @Override
    public List<WishlistDTO> getWishlistByUserId(String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
        logger.info("Fetched wishlist for user ID {}", userId);

        return wishlists.stream()
                .map(wishlist -> new WishlistDTO(wishlist.getId(), wishlist.getProduct().getId(), wishlist.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<WishlistDTO> getAllWishlists(String token) {
        authorizationUtils.checkAdminRole(token);
        List<Wishlist> wishlists = wishlistRepository.findAll();
        logger.info("Fetched all wishlists");

        return wishlists.stream()
                .map(wishlist -> new WishlistDTO(wishlist.getId(), wishlist.getProduct().getId(), wishlist.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "allProducts", allEntries = true)
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
