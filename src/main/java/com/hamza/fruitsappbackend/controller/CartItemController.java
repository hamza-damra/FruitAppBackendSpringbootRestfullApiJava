package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    private final CartItemService cartItemService;

    @Autowired
    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PostMapping
    public ResponseEntity<CartItemDTO> createCartItem(@RequestBody CartItemDTO cartItemDTO) {
        CartItemDTO savedCartItem = cartItemService.saveCartItem(cartItemDTO);
        return ResponseEntity.ok(savedCartItem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartItemDTO> getCartItemById(@PathVariable Long id) {
        Optional<CartItemDTO> cartItemDTO = cartItemService.getCartItemById(id);
        return cartItemDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<List<CartItemDTO>> getCartItemsByCartId(@PathVariable Long cartId) {
        List<CartItemDTO> cartItems = cartItemService.getCartItemsByCartId(cartId);
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getAllCartItems() {
        List<CartItemDTO> cartItems = cartItemService.getAllCartItems();
        return ResponseEntity.ok(cartItems);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartItemDTO> updateCartItem(@PathVariable Long id, @RequestBody CartItemDTO cartItemDTO) {
        cartItemDTO.setId(id);
        CartItemDTO updatedCartItem = cartItemService.updateCartItem(cartItemDTO);
        return updatedCartItem != null ? ResponseEntity.ok(updatedCartItem) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartItemById(@PathVariable Long id) {
        cartItemService.deleteCartItemById(id);
        return ResponseEntity.noContent().build();
    }
}
