package com.hamza.fruitsappbackend.modules.wishlist.controller;

import com.hamza.fruitsappbackend.modules.wishlist.dto.WishlistResponse;
import com.hamza.fruitsappbackend.modules.wishlist.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @Autowired
    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToWishlist(@RequestHeader("Authorization") String token,
                                                @RequestParam Long productId) {
        wishlistService.addToWishlist(productId, token);
        return ResponseEntity.ok("Product added to wishlist");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFromWishlist(@RequestHeader("Authorization") String token,
                                                     @RequestParam Long productId) {
        wishlistService.removeFromWishlist(productId, token);
        return ResponseEntity.ok("Product removed from wishlist");
    }

    @DeleteMapping("/all/remove")
    public ResponseEntity<String> removeAllFromWishlist(@RequestHeader("Authorization") String token) {
        wishlistService.removeAllFromWishlist(token);
        return ResponseEntity.ok("All products removed from wishlist");
    }

    @GetMapping("/user")
    public ResponseEntity<WishlistResponse> getWishlist(@RequestHeader("Authorization") String token) {
        WishlistResponse wishlistItems = wishlistService.getWishlistByUserId(token);
        return ResponseEntity.ok(wishlistItems);
    }

    @GetMapping("/all")
    public ResponseEntity<WishlistResponse> getAllWishlists(@RequestHeader("Authorization") String token) {
        WishlistResponse wishlists = wishlistService.getAllWishlists(token);
        return ResponseEntity.ok(wishlists);
    }
}
