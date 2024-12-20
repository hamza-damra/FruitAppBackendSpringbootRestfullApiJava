package com.hamza.fruitsappbackend.modules.cart.service_impl;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.exception.global.BadRequestException;
import com.hamza.fruitsappbackend.modules.cart.dto.CartItemDTO;
import com.hamza.fruitsappbackend.modules.cart.dto.CartItemResponseDto;
import com.hamza.fruitsappbackend.modules.cart.dto.CartResponseDto;
import com.hamza.fruitsappbackend.modules.cart.entity.Cart;
import com.hamza.fruitsappbackend.modules.cart.entity.CartItem;
import com.hamza.fruitsappbackend.modules.product.entity.Product;
import com.hamza.fruitsappbackend.modules.cart.exception.CartItemNotFoundException;
import com.hamza.fruitsappbackend.modules.cart.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.modules.product.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.modules.cart.repository.CartItemRepository;
import com.hamza.fruitsappbackend.modules.cart.repository.CartRepository;
import com.hamza.fruitsappbackend.modules.product.repository.ProductRepository;
import com.hamza.fruitsappbackend.modules.cart.service.CartItemService;
import com.hamza.fruitsappbackend.modules.user.repository.UserRepository;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;
    private final UserRepository userRepository;

    @Autowired
    public CartItemServiceImpl(CartItemRepository cartItemRepository, CartRepository cartRepository,
                               ProductRepository productRepository, ModelMapper modelMapper,
                               AuthorizationUtils authorizationUtils, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
        this.userRepository = userRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "allWishlists", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    @Transactional
    public CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        if (cart.getStatus() == CartStatus.COMPLETED) {
            cart.reopenCart();
        }

        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", cartItemDTO.getProductId().toString()));

        if (cartItemRepository.existsByCartIdAndProductId(cart.getId(), product.getId())) {
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

        // Ensure only one active cart is fetched
        Cart cart = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("productId", productId.toString()));

        return modelMapper.map(cartItem, CartItemDTO.class);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "allWishlists", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    @Transactional
    public CartItemDTO updateCartItem(Long cartId, CartItemDTO cartItemDTO, String token) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        if (cart.getStatus() == CartStatus.COMPLETED) {
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
    @Caching(evict = {
            @CacheEvict(value = "allWishlists", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    @Transactional
    public CartItemResponseDto deleteCartItemByProductId(Long productId, String token) {
        Long userId = getUserIdAndCheckRole(token);

        Cart cart = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("productId", productId.toString()));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        updateCartTotal(cart);

        List<CartItemDTO> cartItemDTOS = cart.getCartItems().stream().map(this::convertToDTO).toList();

        return new CartItemResponseDto(cart.getCartItems().size(), cartItemDTOS);
    }

    @Override
    public CartResponseDto getCartItemsByUser(String token) {
        Long userId = getUserIdAndCheckRole(token);

        // Attempt to fetch an active cart for the user
        Cart cart = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    // If no active cart is found, create a new one
                    Cart newCart = new Cart();
                    newCart.setUser(userRepository.getUserById(userId));
                    newCart.setStatus(CartStatus.ACTIVE);
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    newCart.setTotalQuantity(0);
                    return cartRepository.save(newCart);
                });

        // Sort the cart items by creation date in descending order
        List<CartItem> sortedCartItems = cart.getCartItems().stream()
                .sorted(Comparator.comparing(CartItem::getCreatedAt).reversed())
                .toList();

        // Calculate the total price for the cart items
        BigDecimal totalPrice = sortedCartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        // Return the cart response
        return new CartResponseDto(
                totalPrice,
                sortedCartItems.size(),
                sortedCartItems.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList())
        );
    }


    @Override
    @Caching(evict = {
            @CacheEvict(value = "allWishlists", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    @Transactional
    public void deleteAllCartItemsByUser(String token) {
        Long userId = getUserIdAndCheckRole(token);

        Cart cart = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        cartItemRepository.deleteAllByCartId(cart.getId());

        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setTotalQuantity(0);
        cartRepository.save(cart);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "allWishlists", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    @Transactional
    public CartItemDTO increaseCartItemQuantity(Long productId, String token) {
        Long userId = getUserIdAndCheckRole(token);

        Cart cart = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("userId", userId.toString()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("productId", productId.toString()));

        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItemRepository.save(cartItem);

        updateCartTotal(cart);

        return convertToDTO(cartItem);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "allWishlists", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    @Transactional
    public CartItemDTO decreaseCartItemQuantity(Long productId, String token) {
        Long userId = getUserIdAndCheckRole(token);

        Cart cart = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .stream()
                .findFirst()
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
                .orElseThrow(() -> new CartNotFoundException("id", cart.getId().toString()));

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
