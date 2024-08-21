package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.CartDTO;
import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.entity.Cart;
import com.hamza.fruitsappbackend.entity.CartItem;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.exception.CartItemNotFoundException;
import com.hamza.fruitsappbackend.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.repository.CartItemRepository;
import com.hamza.fruitsappbackend.repository.CartRepository;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import com.hamza.fruitsappbackend.service.CartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository,
                           ProductRepository productRepository, CartItemRepository cartItemRepository,
                           ModelMapper modelMapper, JwtTokenProvider jwtTokenProvider) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.modelMapper = modelMapper;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private void checkUserOrAdminRole(String token, Long userId) {
        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (!user.getId().equals(userId) && user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }
    }

    private void checkAdminRole(String token) {
        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }
    }

    @Override
    @Transactional
    public CartDTO saveCart(CartDTO cartDTO, String token) {
        checkUserOrAdminRole(token, cartDTO.getUserId());

        Cart cart = createOrUpdateCart(cartDTO);
        Cart savedCart = cartRepository.save(cart);
        List<CartItemDTO> savedCartItems = processCartItems(cartDTO, savedCart);
        return mapCartToDTO(savedCart, savedCartItems);
    }

    @Override
    public CartDTO updateCart(CartDTO cartDTO, String token) {
        Cart cart = cartRepository.findById(cartDTO.getId())
                .orElseThrow(() -> new CartNotFoundException("id", cartDTO.getId().toString()));

        checkUserOrAdminRole(token, cart.getUser().getId());

        setUser(cartDTO, cart);
        cart.getCartItems().clear();

        List<CartItemDTO> savedCartItems = processCartItems(cartDTO, cart);
        Cart updatedCart = cartRepository.save(cart);

        return mapCartToDTO(updatedCart, savedCartItems);
    }

    @Override
    public CartItemDTO addCartItemToCart(Long cartId, CartItemDTO cartItemDTO, String token) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        checkUserOrAdminRole(token, cart.getUser().getId());

        CartItem cartItem = createCartItem(cartItemDTO, cart);
        CartItem savedCartItem = cartItemRepository.save(cartItem);

        return modelMapper.map(savedCartItem, CartItemDTO.class);
    }

    @Override
    public void removeCartItemFromCart(Long cartId, Long cartItemId, String token) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        checkUserOrAdminRole(token, cart.getUser().getId());

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("id", cartItemId.toString()));

        if (!cart.getCartItems().contains(cartItem)) {
            throw new RuntimeException("CartItem does not belong to the specified cart");
        }

        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);
    }

    @Override
    public Optional<CartDTO> getCartById(Long id, String token) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new CartNotFoundException("id", id.toString()));

        checkUserOrAdminRole(token, cart.getUser().getId());

        return Optional.of(modelMapper.map(cart, CartDTO.class));
    }

    @Override
    public List<CartDTO> getCartsByUserId(Long userId, String token) {
        checkUserOrAdminRole(token, userId);

        return cartRepository.findByUserId(userId).stream()
                .map(cart -> modelMapper.map(cart, CartDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CartDTO> getAllCarts(String token) {
        checkAdminRole(token);

        return cartRepository.findAll().stream()
                .map(cart -> modelMapper.map(cart, CartDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCartById(Long id, String token) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new CartNotFoundException("id", id.toString()));

        checkUserOrAdminRole(token, cart.getUser().getId());

        cartRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteCartsByUserId(Long userId, String token) {
        checkAdminRole(token);

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

    private List<CartItemDTO> processCartItems(CartDTO cartDTO, Cart cart) {
        Map<Long, Integer> productQuantities = cartDTO.getCartItems().stream()
                .collect(Collectors.toMap(
                        CartItemDTO::getProductId,
                        CartItemDTO::getQuantity,
                        Integer::sum
                ));

        return productQuantities.entrySet().stream()
                .map(entry -> {
                    CartItem cartItem = createCartItem(entry, cart);
                    CartItem savedCartItem = cartItemRepository.save(cartItem);
                    return modelMapper.map(savedCartItem, CartItemDTO.class);
                })
                .collect(Collectors.toList());
    }

    private CartItem createCartItem(Map.Entry<Long, Integer> entry, Cart cart) {
        CartItem cartItem = new CartItem();
        cartItem.setProduct(productRepository.findById(entry.getKey())
                .orElseThrow(() -> new ProductNotFoundException("id", entry.getKey().toString())));
        cartItem.setQuantity(entry.getValue());
        cartItem.setPrice(BigDecimal.valueOf(cartItem.getProduct().getPrice()));
        cartItem.setCart(cart);
        return cartItem;
    }

    private CartItem createCartItem(CartItemDTO cartItemDTO, Cart cart) {
        CartItem cartItem = new CartItem();
        cartItem.setProduct(productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", cartItemDTO.getProductId().toString())));
        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItem.setPrice(BigDecimal.valueOf(cartItem.getProduct().getPrice()));
        cartItem.setCart(cart);
        return cartItem;
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
