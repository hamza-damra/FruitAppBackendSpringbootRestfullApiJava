package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.WishlistDTO;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.entity.Wishlist;
import com.hamza.fruitsappbackend.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.exception.WishlistNotFoundException;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.repository.WishlistRepository;
import com.hamza.fruitsappbackend.service.WishlistService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

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
    public void addToWishlist(Long productId, String token) {
        Long userId = Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));

        authorizationUtils.checkUserOrAdminRole(token, userId);

        if (wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
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
    }

    @Override
    public void removeFromWishlist(Long productId, String token) {
        Long userId = Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));

        authorizationUtils.checkUserOrAdminRole(token, userId);

        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new WishlistNotFoundException("user_id and product_id", userId.toString() + " " + productId.toString()));
        wishlistRepository.delete(wishlist);
    }

    @Override
    public List<WishlistDTO> getWishlistByUserId(String token) {
        Long userId = Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));

        authorizationUtils.checkUserOrAdminRole(token, userId);

        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
        return wishlists.stream()
                .map(wishlist -> new WishlistDTO(wishlist.getId(), wishlist.getProduct().getId(), wishlist.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<WishlistDTO> getAllWishlists(String token) {
        authorizationUtils.checkAdminRole(token);
        List<Wishlist> wishlists = wishlistRepository.findAll();
        return wishlists.stream()
                .map(wishlist -> new WishlistDTO(wishlist.getId(), wishlist.getProduct().getId(), wishlist.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
