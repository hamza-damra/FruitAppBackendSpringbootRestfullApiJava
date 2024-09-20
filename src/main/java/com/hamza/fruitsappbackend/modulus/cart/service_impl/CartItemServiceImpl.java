package com.hamza.fruitsappbackend.modulus.cart.service_impl;

import com.hamza.fruitsappbackend.exception.global.BadRequestException;
import com.hamza.fruitsappbackend.modulus.cart.dto.CartItemDTO;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    @CachePut(value = "cartItems", key = "#cartItemDTO.productId")
    public CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token) {

        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

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
    @Cacheable(value = "cartItems", key = "#productId")
    public CartItemDTO getCartItemByProductId(Long productId, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("productId", productId.toString()));

        return modelMapper.map(cartItem, CartItemDTO.class);
    }

    @Override
    @CachePut(value = "cartItems", key = "#cartItemDTO.productId")
    public CartItemDTO updateCartItem(Long cartId, CartItemDTO cartItemDTO, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        CartItem existingCartItem = cartItemRepository.findById(cartItemDTO.getId())
                .orElseThrow(() -> new CartItemNotFoundException("id", cartItemDTO.getId().toString()));

        existingCartItem.setQuantity(cartItemDTO.getQuantity());
        cartItemRepository.save(existingCartItem);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));
        updateCartTotal(cart);

        return convertToDTO(existingCartItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cartItems", key = "#productId")
    public void deleteCartItemByProductId(Long productId, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("productId", productId.toString()));

        cartItemRepository.delete(cartItem);

        updateCartTotal(cart);
    }

    @Override
    public List<CartItemDTO> getCartItemsByUser(String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        List<CartItem> cartItems = cart.getCartItems();

        return cartItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllCartItemsByUser(String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

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
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("productId", productId.toString()));

        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItemRepository.save(cartItem);

        updateCartTotal(cart);

        return convertToDTO(cartItem);
    }

    private void updateCartTotal(Cart cart) {
        BigDecimal totalPrice = cart.getCartItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        cart.setTotalPrice(totalPrice);
        cart.setTotalQuantity(totalQuantity);
        cartRepository.save(cart);
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
        cartItemDTO.setProductId(cartItem.getProduct().getId());
        cartItemDTO.setQuantity(cartItem.getQuantity());
        cartItemDTO.setPrice(cartItem.getProduct().getPrice());
        cartItemDTO.setProductName(cartItem.getProduct().getName());
        cartItemDTO.setProductImageUrl(cartItem.getProduct().getImageUrl());
        return cartItemDTO;
    }
}
