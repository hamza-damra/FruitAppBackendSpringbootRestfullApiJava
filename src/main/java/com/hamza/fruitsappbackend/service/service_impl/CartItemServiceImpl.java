package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.entity.CartItem;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.repository.CartItemRepository;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.service.CartItemService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CartItemServiceImpl(CartItemRepository cartItemRepository, ProductRepository productRepository, ModelMapper modelMapper) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CartItemDTO saveCartItem(CartItemDTO cartItemDTO) {
        // Retrieve the Product by ID
        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found for the given ID: " + cartItemDTO.getProductId()));

        // Create the CartItem entity manually
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItem.setPrice(BigDecimal.valueOf(product.getPrice()));

        // Ensure that the Cart is set before saving the CartItem
        if (cartItem.getCart() == null) {
            throw new RuntimeException("Cart not set in CartItem. This indicates that the CartItem is not properly associated with a Cart.");
        }

        // Save the CartItem
        CartItem savedCartItem = cartItemRepository.save(cartItem);

        // Map the saved entity back to DTO to ensure the ID is captured
        return modelMapper.map(savedCartItem, CartItemDTO.class);
    }

    @Override
    public Optional<CartItemDTO> getCartItemById(Long id) {
        return cartItemRepository.findById(id)
                .map(cartItem -> {
                    CartItemDTO cartItemDTO = modelMapper.map(cartItem, CartItemDTO.class);
                    cartItemDTO.setProductId(cartItem.getProduct().getId());
                    return cartItemDTO;
                });
    }

    @Override
    public List<CartItemDTO> getCartItemsByCartId(Long cartId) {
        return cartItemRepository.findByCartId(cartId).stream()
                .map(cartItem -> {
                    CartItemDTO cartItemDTO = modelMapper.map(cartItem, CartItemDTO.class);
                    cartItemDTO.setProductId(cartItem.getProduct().getId());
                    return cartItemDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CartItemDTO> getAllCartItems() {
        return cartItemRepository.findAll().stream()
                .map(cartItem -> {
                    CartItemDTO cartItemDTO = modelMapper.map(cartItem, CartItemDTO.class);
                    cartItemDTO.setProductId(cartItem.getProduct().getId());
                    return cartItemDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CartItemDTO updateCartItem(CartItemDTO cartItemDTO) {
        Optional<CartItem> existingCartItemOptional = cartItemRepository.findById(cartItemDTO.getId());
        if (existingCartItemOptional.isPresent()) {
            CartItem existingCartItem = existingCartItemOptional.get();

            // Update quantity
            if (cartItemDTO.getQuantity() > 0) {
                existingCartItem.setQuantity(cartItemDTO.getQuantity());
            }

            // Update product
            if (cartItemDTO.getProductId() != null) {
                Product product = productRepository.findById(cartItemDTO.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found for the given ID: " + cartItemDTO.getProductId()));
                existingCartItem.setProduct(product);
                existingCartItem.setPrice(BigDecimal.valueOf(product.getPrice()));
            }

            CartItem updatedCartItem = cartItemRepository.save(existingCartItem);
            return modelMapper.map(updatedCartItem, CartItemDTO.class);
        } else {
            throw new RuntimeException("CartItem not found with ID: " + cartItemDTO.getId());
        }
    }

    @Override
    public void deleteCartItemById(Long id) {
        cartItemRepository.deleteById(id);
    }
}
