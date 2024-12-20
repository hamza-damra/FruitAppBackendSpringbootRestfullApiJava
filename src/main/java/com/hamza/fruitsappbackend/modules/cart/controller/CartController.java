package com.hamza.fruitsappbackend.modules.cart.controller;

import com.hamza.fruitsappbackend.modules.cart.dto.CartItemDTO;
import com.hamza.fruitsappbackend.modules.cart.dto.CartItemResponseDto;
import com.hamza.fruitsappbackend.modules.cart.dto.CartResponseDto;
import com.hamza.fruitsappbackend.modules.cart.service.CartItemService;
import com.hamza.fruitsappbackend.modules.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
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
    public ResponseEntity<CartItemDTO> addItemToCart(@RequestHeader("Authorization") String token,
                                                     @Valid @RequestBody CartItemDTO cartItemDTO) {
        CartItemDTO addedCartItem = cartService.addCartItemToCart(cartItemDTO, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCartItem);
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeCart(@RequestHeader("Authorization") String token) {
        cartService.completeCart(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reopen")
    public ResponseEntity<Void> reopenCart(@RequestHeader("Authorization") String token) {
        cartService.reopenCart(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get")
    public ResponseEntity<CartResponseDto> getCartItemsForUser(@RequestHeader("Authorization") String token) {
        CartResponseDto cartItems = cartItemService.getCartItemsByUser(token);
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
    public ResponseEntity<CartItemResponseDto> removeItemFromCart(@PathVariable Long productId,
                                                                  @RequestHeader("Authorization") String token) {
       CartItemResponseDto cartItemResponseDto =  cartItemService.deleteCartItemByProductId(productId, token);
        return ResponseEntity.ok(cartItemResponseDto);
    }

    @DeleteMapping("/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeAllCartItems(@RequestHeader("Authorization") String token) {
        cartItemService.deleteAllCartItemsByUser(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/increase-quantity/{productId}")
    public ResponseEntity<CartItemDTO> increaseCartItemQuantity(@PathVariable Long productId,
                                                                 @RequestHeader("Authorization") String token) {
        CartItemDTO updatedCartItem = cartItemService.increaseCartItemQuantity(productId, token);
        return ResponseEntity.ok(updatedCartItem);
    }


    @PostMapping("/decrease-quantity/{productId}")
    public ResponseEntity<CartItemDTO> decreaseCartItemQuantity(@PathVariable Long productId,
                                                                @RequestHeader("Authorization") String token) {
        CartItemDTO updatedCartItem = cartItemService.decreaseCartItemQuantity(productId, token);
        return ResponseEntity.ok(updatedCartItem);
    }
}
