package com.hamza.fruitsappbackend.modulus.wishlist.service;

import com.hamza.fruitsappbackend.modulus.wishlist.dto.WishlistDTO;

import java.util.List;

public interface WishlistService {
    void addToWishlist(Long productId, String token);
    void removeFromWishlist(Long productId, String token);
    List<WishlistDTO> getWishlistByUserId(String token);
    List<WishlistDTO> getAllWishlists(String token);
}
