package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.CartDTO;
import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.entity.Cart;
import com.hamza.fruitsappbackend.entity.CartItem;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.repository.CartItemRepository;
import com.hamza.fruitsappbackend.repository.CartRepository;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.CartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository,
                           ProductRepository productRepository, CartItemRepository cartItemRepository,
                           ModelMapper modelMapper) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public CartDTO saveCart(CartDTO cartDTO) {
        Cart cart = modelMapper.map(cartDTO, Cart.class);

        User user = userRepository.findById(cartDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + cartDTO.getUserId()));
        cart.setUser(user);

        Cart savedCart = cartRepository.save(cart);

        Map<Long, Integer> productQuantities = cartDTO.getCartItems().stream()
                .collect(Collectors.toMap(
                        CartItemDTO::getProductId,
                        CartItemDTO::getQuantity,
                        Integer::sum
                ));

        List<CartItemDTO> savedCartItems = productQuantities.entrySet().stream()
                .map(entry -> {
                    CartItem cartItem = new CartItem();
                    cartItem.setProduct(productRepository.findById(entry.getKey())
                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + entry.getKey())));
                    cartItem.setQuantity(entry.getValue());
                    cartItem.setPrice(BigDecimal.valueOf(cartItem.getProduct().getPrice()));
                    cartItem.setCart(savedCart);

                    CartItem savedCartItem = cartItemRepository.save(cartItem);
                    return modelMapper.map(savedCartItem, CartItemDTO.class);
                })
                .collect(Collectors.toList());

        CartDTO savedCartDTO = modelMapper.map(savedCart, CartDTO.class);
        savedCartDTO.setCartItems(savedCartItems);

        return savedCartDTO;
    }

    @Override
    public CartDTO updateCart(CartDTO cartDTO) {
        Cart cart = cartRepository.findById(cartDTO.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found with ID: " + cartDTO.getId()));

        User user = userRepository.findById(cartDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + cartDTO.getUserId()));
        cart.setUser(user);

        cart.getCartItems().clear();

        List<CartItemDTO> savedCartItems = cartDTO.getCartItems().stream()
                .map(cartItemDTO -> {
                    CartItem cartItem = new CartItem();
                    cartItem.setProduct(productRepository.findById(cartItemDTO.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + cartItemDTO.getProductId())));
                    cartItem.setQuantity(cartItemDTO.getQuantity());
                    cartItem.setPrice(BigDecimal.valueOf(cartItem.getProduct().getPrice()));
                    cartItem.setCart(cart);  // Set the reference to the existing cart

                    CartItem savedCartItem = cartItemRepository.save(cartItem);
                    return modelMapper.map(savedCartItem, CartItemDTO.class);
                })
                .collect(Collectors.toList());

        Cart updatedCart = cartRepository.save(cart);
        CartDTO updatedCartDTO = modelMapper.map(updatedCart, CartDTO.class);
        updatedCartDTO.setCartItems(savedCartItems);

        return updatedCartDTO;
    }


    private void setUser(CartDTO cartDTO, Cart cart) {
        User user = userRepository.findById(cartDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + cartDTO.getUserId()));
        cart.setUser(user);
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

    @Override
    public CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with ID: " + cartId));

        CartItem cartItem = new CartItem();
        cartItem.setProduct(productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + cartItemDTO.getProductId())));
        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItem.setPrice(BigDecimal.valueOf(cartItem.getProduct().getPrice()));
        cartItem.setCart(cart);

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return modelMapper.map(savedCartItem, CartItemDTO.class);
    }

    @Override
    public void removeCartItemFromCart(Long cartId, Long cartItemId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with ID: " + cartId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found with ID: " + cartItemId));

        if (!cart.getCartItems().contains(cartItem)) {
            throw new RuntimeException("CartItem does not belong to the specified cart");
        }

        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);
    }
}
