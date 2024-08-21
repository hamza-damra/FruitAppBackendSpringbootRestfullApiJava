package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.CartDTO;
import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.service.CartService;
import com.hamza.fruitsappbackend.validators.markers.OnCreate;
import com.hamza.fruitsappbackend.validators.markers.OnUpdate;
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
    @Validated(OnCreate.class)
    public ResponseEntity<CartDTO> createCart(@RequestHeader("Authorization") String token,
                                              @Validated(OnCreate.class) @RequestBody CartDTO cartDTO) {
        CartDTO savedCart = cartService.saveCart(cartDTO, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCart);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartDTO> getCartById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Optional<CartDTO> cartDTO = cartService.getCartById(id, token);
        return cartDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartDTO>> getCartsByUserId(@RequestHeader("Authorization") String token, @PathVariable Long userId) {
        List<CartDTO> carts = cartService.getCartsByUserId(userId, token);
        return ResponseEntity.ok(carts);
    }

    @GetMapping
    public ResponseEntity<List<CartDTO>> getAllCarts(@RequestHeader("Authorization") String token) {
        List<CartDTO> carts = cartService.getAllCarts(token);
        return ResponseEntity.ok(carts);
    }

    @PutMapping("/{id}")
    @Validated(OnUpdate.class)
    public ResponseEntity<CartDTO> updateCart(@RequestHeader("Authorization") String token,
                                              @PathVariable Long id,
                                              @Validated(OnUpdate.class) @RequestBody CartDTO cartDTO) {
        cartDTO.setId(id);
        CartDTO updatedCart = cartService.updateCart(cartDTO, token);
        return updatedCart != null ? ResponseEntity.ok(updatedCart) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCartById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        cartService.deleteCartById(id, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemDTO> addCartItemToCart(@RequestHeader("Authorization") String token,
                                                         @PathVariable Long cartId,
                                                         @Valid @RequestBody CartItemDTO cartItemDTO) {
        CartItemDTO addedCartItem = cartService.addCartItemToCart(cartId, cartItemDTO, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCartItem);
    }

    @DeleteMapping("/{cartId}/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItemFromCart(@RequestHeader("Authorization") String token,
                                                       @PathVariable Long cartId,
                                                       @PathVariable Long cartItemId) {
        cartService.removeCartItemFromCart(cartId, cartItemId, token);
        return ResponseEntity.noContent().build();
    }
}
