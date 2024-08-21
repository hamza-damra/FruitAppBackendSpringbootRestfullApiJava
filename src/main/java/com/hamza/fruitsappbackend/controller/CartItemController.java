package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/cart/items")
@Validated
public class CartItemController {

    private final CartItemService cartItemService;
    private static final Logger logger = Logger.getLogger(CartItemController.class.getName());


    @Autowired
    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PostMapping("/{cartId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CartItemDTO> createCartItem(@PathVariable Long cartId,
                                                      @Valid @RequestBody CartItemDTO cartItemDTO,
                                                      @RequestHeader("Authorization") String token) {
        CartItemDTO savedCartItem = cartItemService.saveCartItem(cartId, cartItemDTO, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCartItem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartItemDTO> getCartItemById(@PathVariable Long id,
                                                       @RequestHeader("Authorization") String token) {
        Optional<CartItemDTO> cartItemDTO = cartItemService.getCartItemById(id, token);
        return cartItemDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<List<CartItemDTO>> getCartItemsByCartId(@PathVariable Long cartId,
                                                                  @RequestHeader("Authorization") String token) {
        List<CartItemDTO> cartItems = cartItemService.getCartItemsByCartId(cartId, token);
        return ResponseEntity.ok(cartItems);
    }

    @PutMapping("/{cartId}/{id}")
    public ResponseEntity<CartItemDTO> updateCartItem(@PathVariable Long cartId,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody CartItemDTO cartItemDTO,
                                                      @RequestHeader("Authorization") String token) {
        cartItemDTO.setId(id);
        CartItemDTO updatedCartItem = cartItemService.updateCartItem(cartId, cartItemDTO, token);
        return ResponseEntity.ok(updatedCartItem);
    }

    @DeleteMapping("/{cartId}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCartItemById(@PathVariable Long cartId,
                                                   @PathVariable Long id,
                                                   @RequestHeader("Authorization") String token) {
        cartItemService.deleteCartItemById(cartId, id, token);
        return ResponseEntity.noContent().build();
    }
}
