package com.hamza.fruitsappbackend.modulus.cart.controller;

import com.hamza.fruitsappbackend.modulus.cart.dto.CartItemDTO;
import com.hamza.fruitsappbackend.modulus.cart.service.CartItemService;
import com.hamza.fruitsappbackend.modulus.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    @Autowired
    public CartController(CartService cartService, CartItemService cartItemService) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
    }

    // Add an item to the cart
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CartItemDTO> addItemToCart(@RequestHeader("Authorization") String token,
                                                     @Valid @RequestBody CartItemDTO cartItemDTO) {
        CartItemDTO addedCartItem = cartService.addCartItemToCart(null, cartItemDTO, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCartItem);
    }

    @GetMapping("/get")
    public ResponseEntity<List<CartItemDTO>> getCartItemsForUser(@RequestHeader("Authorization") String token) {
        List<CartItemDTO> cartItems = cartItemService.getCartItemsByUser(token);
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<CartItemDTO> getCartItemByProductId(@PathVariable Long productId,
                                                              @RequestHeader("Authorization") String token) {
        CartItemDTO cartItemDTO = cartItemService.getCartItemByProductId(productId, token);
        return ResponseEntity.ok(cartItemDTO);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CartItemDTO> updateCartItemById(@PathVariable Long itemId,
                                                          @Valid @RequestBody CartItemDTO cartItemDTO,
                                                          @RequestHeader("Authorization") String token) {
        cartItemDTO.setId(itemId);
        CartItemDTO updatedCartItem = cartItemService.updateCartItem(itemId, cartItemDTO, token);
        return ResponseEntity.ok(updatedCartItem);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long productId,
                                                   @RequestHeader("Authorization") String token) {
        cartItemService.deleteCartItemByProductId(productId, token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeAllCartItems(@RequestHeader("Authorization") String token) {
        cartItemService.deleteAllCartItemsByUser(token);
        return ResponseEntity.noContent().build();
    }

}
