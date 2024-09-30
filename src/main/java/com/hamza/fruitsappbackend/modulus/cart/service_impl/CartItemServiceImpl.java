package com.hamza.fruitsappbackend.modulus.cart.service_impl;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.exception.global.BadRequestException;
import com.hamza.fruitsappbackend.modulus.cart.dto.CartItemDTO;
import com.hamza.fruitsappbackend.modulus.cart.dto.CartResponseDto;
import com.hamza.fruitsappbackend.modulus.cart.entity.Cart;
import com.hamza.fruitsappbackend.modulus.cart.entity.CartItem;
import com.hamza.fruitsappbackend.modulus.product.entity.Product;
import com.hamza.fruitsappbackend.modulus.cart.exception.CartItemNotFoundException;
import com.hamza.fruitsappbackend.modulus.cart.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.modulus.product.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.modulus.cart.repository.CartItemRepository;
import com.hamza.fruitsappbackend.modulus.cart.repository.CartRepository;
import com.hamza.fruitsappbackend.modulus.product.repository.ProductRepository;
import com.hamza.fruitsappbackend.modulus.cart.service.CartItemService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public CartItemServiceImpl(CartItemRepository cartItemRepository, CartRepository cartRepository,
                               ProductRepository productRepository, ModelMapper modelMapper,
                               AuthorizationUtils authorizationUtils) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    public CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        if(cart.getStatus() == CartStatus.COMPLETED){
            throw new BadRequestException("Cannot add items to a completed cart");
        }

        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", cartItemDTO.getProductId().toString()));

        if(cartItemRepository.existsByCartIdAndProductId(cart.getId(), product.getId())){
            throw new BadRequestException("Item already exists in the cart");
        }

        CartItem cartItem = findOrCreateCartItem(cart, product);
        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItem.setPrice(BigDecimal.valueOf(product.getPrice()));
        cartItemRepository.save(cartItem);

        updateCartTotal(cart);

        return modelMapper.map(cartItem, CartItemDTO.class);
    }

    @Override
    public CartItemDTO getCartItemByProductId(Long productId, String token) {
        Long userId = getUserIdAndCheckRole(token);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("productId", productId.toString()));

        return modelMapper.map(cartItem, CartItemDTO.class);
    }

    @Override
    public CartItemDTO updateCartItem(Long cartId, CartItemDTO cartItemDTO, String token) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        if(cart.getStatus() == CartStatus.COMPLETED){
            throw new BadRequestException("Cannot update items in a completed cart");
        }

        CartItem existingCartItem = cartItemRepository.findById(cartItemDTO.getId())
                .orElseThrow(() -> new CartItemNotFoundException("id", cartItemDTO.getId().toString()));

        existingCartItem.setQuantity(cartItemDTO.getQuantity());
        cartItemRepository.save(existingCartItem);

        updateCartTotal(cart);

        return convertToDTO(existingCartItem);
    }

    @Override
    @Transactional
    public void deleteCartItemByProductId(Long productId, String token) {
        Long userId = getUserIdAndCheckRole(token);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("productId", productId.toString()));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        updateCartTotal(cart);
    }


    @Override
    public CartResponseDto getCartItemsByUser(String token) {
        Long userId = getUserIdAndCheckRole(token);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        List<CartItem> cartItems = cart.getCartItems();

        BigDecimal totalPrice = cartItems.stream().map(CartItem::getPrice).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        return new CartResponseDto(new BigDecimal(totalPrice.toString()),cartItems.size(), cartItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void deleteAllCartItemsByUser(String token) {
        Long userId = getUserIdAndCheckRole(token);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        cartItemRepository.deleteAllByCartId(cart.getId());

        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setTotalQuantity(0);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartItemDTO increaseCartItemQuantity(Long productId, String token) {
        Long userId = getUserIdAndCheckRole(token);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("productId", productId.toString()));

        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItemRepository.save(cartItem);

        updateCartTotal(cart);

        return convertToDTO(cartItem);
    }

    @Override
    @Transactional
    public CartItemDTO decreaseCartItemQuantity(Long productId, String token) {
        Long userId = getUserIdAndCheckRole(token);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("productId", productId.toString()));

        if (cartItem.getQuantity() <= 1) {
            throw new BadRequestException("Cannot decrease quantity below 1");
        }

        cartItem.setQuantity(cartItem.getQuantity() - 1);
        cartItemRepository.save(cartItem);

        updateCartTotal(cart);

        return convertToDTO(cartItem);
    }

    private Long getUserIdAndCheckRole(String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);
        return userId;
    }

    private void updateCartTotal(Cart cart) {
        if (cart.getStatus() == CartStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update total for a completed cart.");
        }

        Cart updatedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new CartNotFoundException("id" , cart.getId().toString()));

        BigDecimal totalPrice = updatedCart.getCartItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = updatedCart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        updatedCart.setTotalPrice(totalPrice);
        updatedCart.setTotalQuantity(totalQuantity);

        cartRepository.save(updatedCart);
    }

    private CartItem findOrCreateCartItem(Cart cart, Product product) {
        return cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> new CartItem(cart, product));
    }

    private Long getUserIdFromToken(String token) {
        return authorizationUtils.getUserFromToken(token).getId();
    }

    private CartItemDTO convertToDTO(CartItem cartItem) {
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(cartItem.getId());
        cartItemDTO.setStockQuantity(cartItem.getProduct().getStockQuantity());
        cartItemDTO.setProductId(cartItem.getProduct().getId());
        cartItemDTO.setQuantity(cartItem.getQuantity());
        cartItemDTO.setPrice(cartItem.getProduct().getPrice());
        cartItemDTO.setProductName(cartItem.getProduct().getName());
        cartItemDTO.setProductImageUrl(cartItem.getProduct().getImageUrl());
        return cartItemDTO;
    }
}