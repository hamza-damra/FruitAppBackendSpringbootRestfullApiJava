package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.CartItemDTO;
import com.hamza.fruitsappbackend.entity.CartItem;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.exception.CartItemNotFoundException;
import com.hamza.fruitsappbackend.exception.ProductNotFoundException;
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
        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", cartItemDTO.getProductId().toString()));

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItem.setPrice(BigDecimal.valueOf(product.getPrice()));

        if (cartItem.getCart() == null) {
            throw new RuntimeException("Cart not set in CartItem. This indicates that the CartItem is not properly associated with a Cart.");
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);

        return modelMapper.map(savedCartItem, CartItemDTO.class);
    }

    @Override
    public Optional<CartItemDTO> getCartItemById(Long id) {
        return cartItemRepository.findById(id)
                .map(cartItem -> {
                    CartItemDTO cartItemDTO = modelMapper.map(cartItem, CartItemDTO.class);
                    cartItemDTO.setProductId(cartItem.getProduct().getId());
                    return cartItemDTO;
                })
                .or(() -> {
                    throw new CartItemNotFoundException("id", id.toString());
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
        CartItem existingCartItem = cartItemRepository.findById(cartItemDTO.getId())
                .orElseThrow(() -> new CartItemNotFoundException("id", cartItemDTO.getId().toString()));

        if (cartItemDTO.getQuantity() > 0) {
            existingCartItem.setQuantity(cartItemDTO.getQuantity());
        }

        if (cartItemDTO.getProductId() != null) {
            Product product = productRepository.findById(cartItemDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("id", cartItemDTO.getProductId().toString()));
            existingCartItem.setProduct(product);
            existingCartItem.setPrice(BigDecimal.valueOf(product.getPrice()));
        }

        CartItem updatedCartItem = cartItemRepository.save(existingCartItem);
        return modelMapper.map(updatedCartItem, CartItemDTO.class);
    }

    @Override
    public void deleteCartItemById(Long id) {
        if (!cartItemRepository.existsById(id)) {
            throw new CartItemNotFoundException("id", id.toString());
        }
        cartItemRepository.deleteById(id);
    }
}
