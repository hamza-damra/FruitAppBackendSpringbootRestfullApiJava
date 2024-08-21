package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.entity.Cart;
import com.hamza.fruitsappbackend.entity.CartItem;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.exception.CartItemNotFoundException;
import com.hamza.fruitsappbackend.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.repository.CartItemRepository;
import com.hamza.fruitsappbackend.repository.CartRepository;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.service.CartItemService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;
    private static final Logger logger = LoggerFactory.getLogger(CartItemServiceImpl.class);

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
    public CartItemDTO saveCartItem(Long cartId, CartItemDTO cartItemDTO, String token) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        authorizationUtils.checkUserOrAdminRole(token, cart.getUser().getId());

        CartItem cartItem = createOrUpdateCartItem(cartItemDTO, cart);
        CartItem savedCartItem = cartItemRepository.save(cartItem);

        return modelMapper.map(savedCartItem, CartItemDTO.class);
    }

    @Override
    public Optional<CartItemDTO> getCartItemById(Long id, String token) {
        token = token.trim();
        System.out.println("Received token: "+ token);

        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new CartItemNotFoundException("id", id.toString()));

        authorizationUtils.checkUserOrAdminRole(token, cartItem.getCart().getUser().getId());

        return Optional.of(modelMapper.map(cartItem, CartItemDTO.class));
    }

    @Override
    public List<CartItemDTO> getCartItemsByCartId(Long cartId, String token) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        authorizationUtils.checkUserOrAdminRole(token, cart.getUser().getId());

        return cartItemRepository.findByCartId(cartId).stream()
                .map(cartItem -> modelMapper.map(cartItem, CartItemDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CartItemDTO> getAllCartItems(String token) {
        token = token.trim();
        System.out.println("Received token: "+ token);

        authorizationUtils.checkAdminRole(token);

        return cartItemRepository.findAll().stream()
                .map(cartItem -> modelMapper.map(cartItem, CartItemDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CartItemDTO updateCartItem(Long cartId, CartItemDTO cartItemDTO, String token) {
        token = token.trim();
        System.out.println("Received token: "+ token);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        authorizationUtils.checkUserOrAdminRole(token, cart.getUser().getId());

        CartItem existingCartItem = cartItemRepository.findById(cartItemDTO.getId())
                .orElseThrow(() -> new CartItemNotFoundException("id", cartItemDTO.getId().toString()));

        if (!existingCartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to the specified cart");
        }

        CartItem updatedCartItem = createOrUpdateCartItem(cartItemDTO, cart);
        cartItemRepository.save(updatedCartItem);

        return modelMapper.map(updatedCartItem, CartItemDTO.class);
    }

    @Override
    public void deleteCartItemById(Long cartId, Long id, String token) {
        token = token.trim();
        System.out.println("Received token: "+ token);

        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new CartItemNotFoundException("id", id.toString()));

        authorizationUtils.checkUserOrAdminRole(token, cartItem.getCart().getUser().getId());

        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new RuntimeException("CartItem does not belong to the specified cart");
        }

        cartItemRepository.deleteById(id);
    }

    private CartItem createOrUpdateCartItem(CartItemDTO cartItemDTO, Cart cart) {
        CartItem cartItem = cartItemDTO.getId() != null ?
                cartItemRepository.findById(cartItemDTO.getId())
                        .orElse(new CartItem()) : new CartItem();

        cartItem.setCart(cart);
        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", cartItemDTO.getProductId().toString()));
        cartItem.setProduct(product);
        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItem.setPrice(BigDecimal.valueOf(product.getPrice()));

        return cartItem;
    }
}
