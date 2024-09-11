package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.entity.Cart;
import com.hamza.fruitsappbackend.entity.CartItem;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.exception.CartItemNotFoundException;
import com.hamza.fruitsappbackend.exception.CartNotFoundException;
import com.hamza.fruitsappbackend.exception.InsufficientStockException;
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

        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", cartItemDTO.getProductId().toString()));

        if (product.getStockQuantity() < cartItemDTO.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }


        CartItem cartItem = findOrCreateCartItem(cart, product);
        cartItem.setQuantity(cartItem.getQuantity() + cartItemDTO.getQuantity());
        cartItem.setPrice(BigDecimal.valueOf(product.getPrice()));
        cartItemRepository.save(cartItem);
        product.setInCart(true);
        productRepository.save(product);

        return modelMapper.map(cartItem, CartItemDTO.class);
    }

    @Override
    public Optional<CartItemDTO> getCartItemById(Long id, String token) {
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
        authorizationUtils.checkAdminRole(token);

        return cartItemRepository.findAll().stream()
                .map(cartItem -> modelMapper.map(cartItem, CartItemDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CartItemDTO updateCartItem(Long cartId, CartItemDTO cartItemDTO, String token) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("id", cartId.toString()));

        authorizationUtils.checkUserOrAdminRole(token, cart.getUser().getId());

        CartItem existingCartItem = cartItemRepository.findById(cartItemDTO.getId())
                .orElseThrow(() -> new CartItemNotFoundException("id", cartItemDTO.getId().toString()));

        if (!existingCartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to the specified cart");
        }

        validateStockAvailability(existingCartItem.getProduct(), cartItemDTO.getQuantity());

        existingCartItem.setQuantity(cartItemDTO.getQuantity());
        cartItemRepository.save(existingCartItem);

        return modelMapper.map(existingCartItem, CartItemDTO.class);
    }

    @Override
    public void deleteCartItemById(Long cartId, Long id, String token) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new CartItemNotFoundException("id", id.toString()));

        authorizationUtils.checkUserOrAdminRole(token, cartItem.getCart().getUser().getId());

        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new RuntimeException("CartItem does not belong to the specified cart");
        }

        Product product = cartItem.getProduct();
        product.setInCart(false);
        productRepository.save(product);

        cartItemRepository.deleteById(id);
    }

    private CartItem findOrCreateCartItem(Cart cart, Product product) {
        return cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> new CartItem(cart, product));
    }

    private void validateStockAvailability(Product product, int requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }
    }
}
