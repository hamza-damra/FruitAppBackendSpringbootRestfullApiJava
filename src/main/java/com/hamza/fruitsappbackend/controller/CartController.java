package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.CartDTO;
import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
@Validated
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CartDTO> createCart(@Valid @RequestBody CartDTO cartDTO) {
        CartDTO savedCart = cartService.saveCart(cartDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCart);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartDTO> getCartById(@PathVariable Long id) {
        Optional<CartDTO> cartDTO = cartService.getCartById(id);
        return cartDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartDTO>> getCartsByUserId(@PathVariable Long userId) {
        List<CartDTO> carts = cartService.getCartsByUserId(userId);
        return ResponseEntity.ok(carts);
    }

    @GetMapping
    public ResponseEntity<List<CartDTO>> getAllCarts() {
        List<CartDTO> carts = cartService.getAllCarts();
        return ResponseEntity.ok(carts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartDTO> updateCart(@PathVariable Long id, @Valid @RequestBody CartDTO cartDTO) {
        cartDTO.setId(id);  // Ensure the cart ID matches the one in the path
        CartDTO updatedCart = cartService.updateCart(cartDTO);
        return updatedCart != null ? ResponseEntity.ok(updatedCart) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCartById(@PathVariable Long id) {
        cartService.deleteCartById(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemDTO> addCartItemToCart(@PathVariable Long cartId, @Valid @RequestBody CartItemDTO cartItemDTO) {
        CartItemDTO addedCartItem = cartService.addCartItemToCart(cartId, cartItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCartItem);
    }


    @DeleteMapping("/{cartId}/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItemFromCart(@PathVariable Long cartId, @PathVariable Long cartItemId) {
        cartService.removeCartItemFromCart(cartId, cartItemId);
        return ResponseEntity.noContent().build();
    }
}
