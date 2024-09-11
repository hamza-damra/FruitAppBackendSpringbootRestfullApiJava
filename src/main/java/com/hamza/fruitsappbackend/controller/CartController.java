package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.CartDTO;
import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.service.CartItemService;
import com.hamza.fruitsappbackend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    @Autowired
    public CartController(CartService cartService, CartItemService cartItemService) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CartDTO> createCart(@RequestHeader("Authorization") String token,
                                              @Valid @RequestBody CartDTO cartDTO) {
        CartDTO savedCart = cartService.saveCart(cartDTO, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCart);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartDTO> getCartById(@RequestHeader("Authorization") String token,
                                               @PathVariable Long id) {
        Optional<CartDTO> cartDTO = cartService.getCartById(id, token);
        return cartDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user")
    public ResponseEntity<CartDTO> getCartByUserId(@RequestHeader("Authorization") String token) {
        CartDTO cart = cartService.getCartByUserId(token);
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    public ResponseEntity<List<CartDTO>> getAllCarts(@RequestHeader("Authorization") String token) {
        List<CartDTO> carts = cartService.getAllCarts(token);
        return ResponseEntity.ok(carts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartDTO> updateCart(@RequestHeader("Authorization") String token,
                                              @PathVariable Long id,
                                              @Valid @RequestBody CartDTO cartDTO) {
        cartDTO.setId(id);
        CartDTO updatedCart = cartService.updateCart(cartDTO, token);
        return updatedCart != null ? ResponseEntity.ok(updatedCart)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCartById(@RequestHeader("Authorization") String token,
                                               @PathVariable Long id) {
        cartService.deleteCartById(id, token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-by-userId")
    public ResponseEntity<Void> deleteCartByUserId(@RequestHeader("Authorization") String token) {
        cartService.deleteCartByUserId(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cartId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CartItemDTO> addCartItemToCart(@RequestHeader("Authorization") String token,
                                                         @PathVariable Long cartId,
                                                         @Valid @RequestBody CartItemDTO cartItemDTO) {
        CartItemDTO addedCartItem = cartService.addCartItemToCart(cartId, cartItemDTO, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCartItem);
    }

    @GetMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartItemDTO> getCartItemById(@PathVariable Long cartId,
                                                       @PathVariable Long itemId,
                                                       @RequestHeader("Authorization") String token) {
        Optional<CartItemDTO> cartItemDTO = cartItemService.getCartItemById(itemId, token);
        return cartItemDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{cartId}/items")
    public ResponseEntity<List<CartItemDTO>> getCartItemsByCartId(@PathVariable Long cartId,
                                                                  @RequestHeader("Authorization") String token) {
        List<CartItemDTO> cartItems = cartItemService.getCartItemsByCartId(cartId, token);
        return ResponseEntity.ok(cartItems);
    }

    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(@PathVariable Long cartId,
                                                      @PathVariable Long itemId,
                                                      @Valid @RequestBody CartItemDTO cartItemDTO,
                                                      @RequestHeader("Authorization") String token) {
        cartItemDTO.setId(itemId);
        CartItemDTO updatedCartItem = cartItemService.updateCartItem(cartId, cartItemDTO, token);
        return ResponseEntity.ok(updatedCartItem);
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCartItemById(@PathVariable Long cartId,
                                                   @PathVariable Long itemId,
                                                   @RequestHeader("Authorization") String token) {
        cartItemService.deleteCartItemById(cartId, itemId, token);
        return ResponseEntity.noContent().build();
    }
}
