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
    public ResponseEntity<CartDTO> createCart(@RequestHeader("Authorization") String token, @Valid @RequestBody CartDTO cartDTO) {
        String jwtToken = token.replace("Bearer ", "");
        CartDTO savedCart = cartService.saveCart(cartDTO, jwtToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCart);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartDTO> getCartById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        Optional<CartDTO> cartDTO = cartService.getCartById(id, jwtToken);
        return cartDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartDTO>> getCartsByUserId(@RequestHeader("Authorization") String token, @PathVariable Long userId) {
        String jwtToken = token.replace("Bearer ", "");
        List<CartDTO> carts = cartService.getCartsByUserId(userId, jwtToken);
        return ResponseEntity.ok(carts);
    }

    @GetMapping
    public ResponseEntity<List<CartDTO>> getAllCarts(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        List<CartDTO> carts = cartService.getAllCarts(jwtToken);
        return ResponseEntity.ok(carts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartDTO> updateCart(@RequestHeader("Authorization") String token, @PathVariable Long id, @Valid @RequestBody CartDTO cartDTO) {
        String jwtToken = token.replace("Bearer ", "");
        cartDTO.setId(id);
        CartDTO updatedCart = cartService.updateCart(cartDTO, jwtToken);
        return updatedCart != null ? ResponseEntity.ok(updatedCart) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCartById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        cartService.deleteCartById(id, jwtToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemDTO> addCartItemToCart(@RequestHeader("Authorization") String token, @PathVariable Long cartId, @Valid @RequestBody CartItemDTO cartItemDTO) {
        String jwtToken = token.replace("Bearer ", "");
        CartItemDTO addedCartItem = cartService.addCartItemToCart(cartId, cartItemDTO, jwtToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCartItem);
    }

    @DeleteMapping("/{cartId}/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItemFromCart(@RequestHeader("Authorization") String token, @PathVariable Long cartId, @PathVariable Long cartItemId) {
        String jwtToken = token.replace("Bearer ", "");
        cartService.removeCartItemFromCart(cartId, cartItemId, jwtToken);
        return ResponseEntity.noContent().build();
    }
}
