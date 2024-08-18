package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.OrderItemDTO;
import com.hamza.fruitsappbackend.entity.OrderItem;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.repository.OrderItemRepository;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.service.OrderItemService;
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
public class OrderItemServiceImpl implements OrderItemService {

    private static final Logger logger = LoggerFactory.getLogger(OrderItemServiceImpl.class);

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public OrderItemServiceImpl(OrderItemRepository orderItemRepository, ProductRepository productRepository, ModelMapper modelMapper) {
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public OrderItemDTO saveOrderItem(OrderItemDTO orderItemDTO) {
        // Retrieve the Product by ID
        Product product = productRepository.findById(orderItemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found for the given ID: " + orderItemDTO.getProductId()));

        // Create the OrderItem entity manually
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(orderItemDTO.getQuantity());
        orderItem.setPrice(BigDecimal.valueOf(product.getPrice()));
        // Note: The order should already be set before calling this service

        // Save the OrderItem
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        // Map the saved entity back to DTO to ensure the ID is captured
        return modelMapper.map(savedOrderItem, OrderItemDTO.class);
    }

    @Override
    public Optional<OrderItemDTO> getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .map(orderItem -> {
                    OrderItemDTO orderItemDTO = new OrderItemDTO();
                    orderItemDTO.setId(orderItem.getId());
                    orderItemDTO.setProductId(orderItem.getProduct().getId());
                    orderItemDTO.setQuantity(orderItem.getQuantity());
                    return orderItemDTO;
                });
    }

    @Override
    public List<OrderItemDTO> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(orderItem -> {
                    OrderItemDTO orderItemDTO = new OrderItemDTO();
                    orderItemDTO.setId(orderItem.getId());
                    orderItemDTO.setProductId(orderItem.getProduct().getId());
                    orderItemDTO.setQuantity(orderItem.getQuantity());
                    return orderItemDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderItemDTO> getAllOrderItems() {
        return orderItemRepository.findAll().stream()
                .map(orderItem -> {
                    OrderItemDTO orderItemDTO = new OrderItemDTO();
                    orderItemDTO.setId(orderItem.getId());
                    orderItemDTO.setProductId(orderItem.getProduct().getId());
                    orderItemDTO.setQuantity(orderItem.getQuantity());
                    return orderItemDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemDTO updateOrderItem(OrderItemDTO orderItemDTO) {
        Optional<OrderItem> existingOrderItemOptional = orderItemRepository.findById(orderItemDTO.getId());
        if (existingOrderItemOptional.isPresent()) {
            OrderItem existingOrderItem = existingOrderItemOptional.get();

            // Update quantity
            if (orderItemDTO.getQuantity() > 0) {
                existingOrderItem.setQuantity(orderItemDTO.getQuantity());
            }

            // Update product
            Product product = productRepository.findById(orderItemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found for the given ID."));
            existingOrderItem.setProduct(product);
            BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
            logger.debug("Updating price for OrderItem: {}", productPrice);
            existingOrderItem.setPrice(productPrice); // Update the price from Product

            OrderItem updatedOrderItem = orderItemRepository.save(existingOrderItem);
            return modelMapper.map(updatedOrderItem, OrderItemDTO.class);
        } else {
            return null;
        }
    }

    @Override
    public void deleteOrderItemById(Long id) {
        orderItemRepository.deleteById(id);
    }
}
