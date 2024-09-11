package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.dto.CartDTO;
import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.entity.Cart;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.exception.BadRequestException;
import com.hamza.fruitsappbackend.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.repository.CartRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.CartItemService;
import com.hamza.fruitsappbackend.service.CartService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartItemService cartItemService;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository,
                           CartItemService cartItemService, ModelMapper modelMapper,
                           AuthorizationUtils authorizationUtils, JwtTokenProvider jwtTokenProvider) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.cartItemService = cartItemService;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public CartDTO saveCart(CartDTO cartDTO, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        if (cartDTO.getId() == null && cartRepository.existsByUserIdAndStatus(userId, CartStatus.ACTIVE)) {
            throw new IllegalStateException("User can have only one active cart.");
        }

        Cart cart = createOrUpdateCart(cartDTO, userId);
        cart.setStatus(CartStatus.ACTIVE);
        Cart savedCart = cartRepository.save(cart);

        List<CartItemDTO> savedCartItems = cartDTO.getCartItems().stream()
                .map(cartItemDTO -> cartItemService.saveCartItem(savedCart.getId(), cartItemDTO, token))
                .collect(Collectors.toList());

        return mapCartToDTO(savedCart, savedCartItems);
    }

    @Override
    @Transactional
    public CartDTO completeCart(Long cartId, String token) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        authorizationUtils.checkUserOrAdminRole(token, cart.getUser().getId());

        cart.setStatus(CartStatus.COMPLETED);
        cartRepository.save(cart);

        return modelMapper.map(cart, CartDTO.class);
    }

    @Override
    @Transactional
    public CartDTO updateCart(CartDTO cartDTO, String token) {
        Long userId = getUserIdFromToken(token);
        Cart cart = cartRepository.findById(cartDTO.getId())
                .orElseThrow(() -> new CartNotFoundException("id", cartDTO.getId().toString()));

        authorizationUtils.checkUserOrAdminRole(token, userId);
        setUser(cart, userId);

        List<CartItemDTO> savedCartItems = cartDTO.getCartItems().stream()
                .map(cartItemDTO -> cartItemService.updateCartItem(cart.getId(), cartItemDTO, token))
                .collect(Collectors.toList());

        Cart updatedCart = cartRepository.save(cart);
        return mapCartToDTO(updatedCart, savedCartItems);
    }

    @Override
    @Transactional
    public CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token) {
        return cartItemService.saveCartItem(cartId, cartItemDTO, token);
    }

    @Override
    @Transactional
    public void removeCartItemFromCart(Long cartId, Long cartItemId, String token) {
        cartItemService.deleteCartItemById(cartId, cartItemId, token);
    }

    @Override
    @Transactional
    public Optional<CartDTO> getCartById(Long id, String token) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new CartNotFoundException("id", id.toString()));

        List<CartItemDTO> cartItems = cartItemService.getCartItemsByCartId(cart.getId(), token);
        return Optional.of(mapCartToDTO(cart, cartItems));
    }

    @Override
    @Transactional
    public CartDTO getCartByUserId(String token) {
        Long userId = getUserIdFromToken(token);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("user_id", userId.toString()));

        List<CartItemDTO> cartItems = cartItemService.getCartItemsByCartId(cart.getId(), token);
        return mapCartToDTO(cart, cartItems);
    }

    @Override
    @Transactional
    public List<CartDTO> getAllCarts(String token) {
        authorizationUtils.checkAdminRole(token);

        return cartRepository.findAll().stream()
                .map(cart -> {
                    List<CartItemDTO> cartItems = cartItemService.getCartItemsByCartId(cart.getId(), token);
                    return mapCartToDTO(cart, cartItems);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCartById(Long id, String token) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new CartNotFoundException("id", id.toString()));

        cartRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteCartByUserId(String token) {
        Long userId = getUserIdFromToken(token);

        if (!cartRepository.existsByUserId(userId)) {
            throw new BadRequestException("Cart does not exist for user.");
        }

        cartRepository.deleteByUserId(userId);
    }

    private Long getUserIdFromToken(String token) {
        return Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));
    }

    private Cart createOrUpdateCart(CartDTO cartDTO, Long userId) {
        Cart cart = modelMapper.map(cartDTO, Cart.class);
        setUser(cart, userId);
        return cart;
    }

    private void setUser(Cart cart, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("id", userId.toString()));
        cart.setUser(user);
    }

    private CartDTO mapCartToDTO(Cart cart, List<CartItemDTO> savedCartItems) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cartDTO.setCartItems(savedCartItems);
        return cartDTO;
    }
}
