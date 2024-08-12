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
        CartItem cartItem = modelMapper.map(cartItemDTO, CartItem.class);

        // Retrieve the Product and its price
        Optional<Product> productOptional = productRepository.findById(cartItem.getProduct().getId());
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            cartItem.setProduct(product); // Set the product in CartItem
        } else {
            throw new RuntimeException("Product not found for the given ID.");
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return modelMapper.map(savedCartItem, CartItemDTO.class);
    }

    @Override
    public Optional<CartItemDTO> getCartItemById(Long id) {
        return cartItemRepository.findById(id)
                .map(cartItem -> {
                    CartItemDTO cartItemDTO = modelMapper.map(cartItem, CartItemDTO.class);
                    cartItemDTO.setPrice(cartItem.getProduct().getPrice()); // Set the price from Product
                    return cartItemDTO;
                });
    }

    @Override
    public List<CartItemDTO> getCartItemsByCartId(Long cartId) {
        return cartItemRepository.findByCartId(cartId).stream()
                .map(cartItem -> modelMapper.map(cartItem, CartItemDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CartItemDTO> getAllCartItems() {
        return cartItemRepository.findAll().stream()
                .map(cartItem -> {
                    CartItemDTO cartItemDTO = modelMapper.map(cartItem, CartItemDTO.class);
                    cartItemDTO.setPrice(cartItem.getProduct().getPrice()); // Set the price from Product
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
            if (cartItemDTO.getProduct() != null) {
                Optional<Product> productOptional = productRepository.findById(cartItemDTO.getProduct().getId());
                if (productOptional.isPresent()) {
                    Product product = productOptional.get();
                    existingCartItem.setProduct(product);
                } else {
                    throw new RuntimeException("Product not found for the given ID.");
                }
            }

            CartItem updatedCartItem = cartItemRepository.save(existingCartItem);
            return modelMapper.map(updatedCartItem, CartItemDTO.class);
        } else {
            return null;
        }
    }

    @Override
    public void deleteCartItemById(Long id) {
        cartItemRepository.deleteById(id);
    }
}
