package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.CartDTO;
import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.entity.Cart;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.repository.CartRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.CartItemService;
import com.hamza.fruitsappbackend.service.CartService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
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

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository,
                           CartItemService cartItemService, ModelMapper modelMapper,
                           AuthorizationUtils authorizationUtils) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.cartItemService = cartItemService;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    @Transactional
    public CartDTO saveCart(CartDTO cartDTO, String token) {
        authorizationUtils.checkUserOrAdminRole(token, cartDTO.getUserId());

        Cart cart = createOrUpdateCart(cartDTO);
        Cart savedCart = cartRepository.save(cart);

        List<CartItemDTO> savedCartItems = cartDTO.getCartItems().stream()
                .map(cartItemDTO -> cartItemService.saveCartItem(savedCart.getId(), cartItemDTO, token))
                .collect(Collectors.toList());

        return mapCartToDTO(savedCart, savedCartItems);
    }

    @Override
    public CartDTO updateCart(CartDTO cartDTO, String token) {
        Cart cart = cartRepository.findById(cartDTO.getId())
                .orElseThrow(() -> new CartNotFoundException("id", cartDTO.getId().toString()));

        authorizationUtils.checkUserOrAdminRole(token, cart.getUser().getId());

        setUser(cartDTO, cart);

        List<CartItemDTO> savedCartItems = cartDTO.getCartItems().stream()
                .map(cartItemDTO -> cartItemService.updateCartItem(cart.getId(), cartItemDTO, token))
                .collect(Collectors.toList());

        Cart updatedCart = cartRepository.save(cart);

        return mapCartToDTO(updatedCart, savedCartItems);
    }

    @Override
    public CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token) {
        return cartItemService.saveCartItem(cartId, cartItemDTO, token);
    }

    @Override
    public void removeCartItemFromCart(Long cartId, Long cartItemId, String token) {
        cartItemService.deleteCartItemById(cartId, cartItemId, token);
    }

    @Override
    public Optional<CartDTO> getCartById(Long id, String token) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new CartNotFoundException("id", id.toString()));

        authorizationUtils.checkUserOrAdminRole(token, cart.getUser().getId());

        List<CartItemDTO> cartItems = cartItemService.getCartItemsByCartId(cart.getId(), token);

        return Optional.of(mapCartToDTO(cart, cartItems));
    }

    @Override
    public List<CartDTO> getCartsByUserId(Long userId, String token) {
        authorizationUtils.checkUserOrAdminRole(token, userId);

        return cartRepository.findByUserId(userId).stream()
                .map(cart -> {
                    List<CartItemDTO> cartItems = cartItemService.getCartItemsByCartId(cart.getId(), token);
                    return mapCartToDTO(cart, cartItems);
                })
                .collect(Collectors.toList());
    }

    @Override
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
    public void deleteCartById(Long id, String token) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new CartNotFoundException("id", id.toString()));

        authorizationUtils.checkUserOrAdminRole(token, cart.getUser().getId());

        cartRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteCartsByUserId(Long userId, String token) {
        authorizationUtils.checkAdminRole(token);

        if (userRepository.existsById(userId)) {
            cartRepository.deleteByUserId(userId);
        } else {
            throw new UserNotFoundException("id", userId.toString());
        }
    }

    private Cart createOrUpdateCart(CartDTO cartDTO) {
        Cart cart = modelMapper.map(cartDTO, Cart.class);
        setUser(cartDTO, cart);
        return cart;
    }

    private void setUser(CartDTO cartDTO, Cart cart) {
        User user = userRepository.findById(cartDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("id", cartDTO.getUserId().toString()));
        cart.setUser(user);
    }

    private CartDTO mapCartToDTO(Cart cart, List<CartItemDTO> savedCartItems) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cartDTO.setCartItems(savedCartItems);
        return cartDTO;
    }
}

