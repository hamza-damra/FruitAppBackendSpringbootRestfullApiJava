package com.hamza.fruitsappbackend.modulus.wishlist.service;

import com.hamza.fruitsappbackend.modulus.wishlist.dto.WishlistDTO;
import com.hamza.fruitsappbackend.modulus.wishlist.dto.WishlistResponse;

import java.util.List;

public interface WishlistService {
    void addToWishlist(Long productId, String token);
    void removeFromWishlist(Long productId, String token);
    WishlistResponse getWishlistByUserId(String token);
    WishlistResponse getAllWishlists(String token);

    void removeAllFromWishlist(String token);
}
