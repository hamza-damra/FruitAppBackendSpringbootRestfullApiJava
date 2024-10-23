package com.hamza.fruitsappbackend.modules.wishlist.service;

import com.hamza.fruitsappbackend.modules.wishlist.dto.WishlistResponse;

public interface WishlistService {
    void addToWishlist(Long productId, String token);
    void removeFromWishlist(Long productId, String token);
    WishlistResponse getWishlistByUserId(String token);
    WishlistResponse getAllWishlists(String token);

    void removeAllFromWishlist(String token);
}
