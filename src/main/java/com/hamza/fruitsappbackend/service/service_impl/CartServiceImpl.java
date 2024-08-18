package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.CartDTO;
import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.entity.Cart;
import com.hamza.fruitsappbackend.entity.CartItem;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.repository.CartRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.service.CartItemService;
import com.hamza.fruitsappbackend.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemService cartItemService;
    private final ModelMapper modelMapper;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository,
                           ProductRepository productRepository, CartItemService cartItemService,
                           ModelMapper modelMapper) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemService = cartItemService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public CartDTO saveCart(CartDTO cartDTO) {
        Cart cart = modelMapper.map(cartDTO, Cart.class);
        setUser(cartDTO, cart);

        // Save the cart first to ensure it has an ID
        Cart savedCart = cartRepository.save(cart);

        // Ensure that cart items are associated with the saved cart
        cartDTO.getCartItems().forEach(cartItemDTO -> {
            CartItem cartItem = modelMapper.map(cartItemDTO, CartItem.class);
            cartItem.setCart(savedCart); // Set the reference to the saved cart

            // Log the price for the cart item
            logger.debug("Saving CartItem with price: {}", cartItem.getPrice());

            cartItemService.saveCartItem(modelMapper.map(cartItem, CartItemDTO.class)); // Save cart item
        });

        return modelMapper.map(savedCart, CartDTO.class);
    }

    @Override
    public CartDTO updateCart(CartDTO cartDTO) {
        Cart cart = cartRepository.findById(cartDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with ID: " + cartDTO.getId()));
        mapCartDetails(cartDTO, cart);
        setUser(cartDTO, cart);

        // Update the cart items
        cart.getCartItems().clear(); // Clear existing items
        cartDTO.getCartItems().forEach(cartItemDTO -> {
            cartItemDTO.setCartId(cart.getId());
            CartItemDTO savedCartItemDTO = cartItemService.saveCartItem(cartItemDTO);
            CartItem cartItem = modelMapper.map(savedCartItemDTO, CartItem.class);

            // Log the price for the updated cart item
            logger.debug("Updating CartItem with price: {}", cartItem.getPrice());

            cart.addCartItem(cartItem);
        });

        Cart updatedCart = cartRepository.save(cart);
        return modelMapper.map(updatedCart, CartDTO.class);
    }

    private void setUser(CartDTO cartDTO, Cart cart) {
        User user = userRepository.findById(cartDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + cartDTO.getUserId()));
        cart.setUser(user);
    }

    private void mapCartDetails(CartDTO cartDTO, Cart cart) {
        // Implement any additional mapping logic if required
    }

    @Override
    public Optional<CartDTO> getCartById(Long id) {
        return cartRepository.findById(id)
                .map(cart -> modelMapper.map(cart, CartDTO.class));
    }

    @Override
    public List<CartDTO> getCartsByUserId(Long userId) {
        return cartRepository.findByUserId(userId).stream()
                .map(cart -> modelMapper.map(cart, CartDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CartDTO> getAllCarts() {
        return cartRepository.findAll().stream()
                .map(cart -> modelMapper.map(cart, CartDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCartById(Long id) {
        cartRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteCartsByUserId(Long userId) {
        if (userRepository.existsById(userId)) {
            cartRepository.deleteByUserId(userId);
        } else {
            throw new RuntimeException("User not found");
        }
    }

}
