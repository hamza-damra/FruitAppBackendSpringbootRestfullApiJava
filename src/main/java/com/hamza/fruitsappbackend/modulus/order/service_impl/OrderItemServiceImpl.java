package com.hamza.fruitsappbackend.modulus.order.service_impl;

import com.hamza.fruitsappbackend.modulus.order.dto.OrderItemDTO;
import com.hamza.fruitsappbackend.modulus.order.entity.OrderItem;
import com.hamza.fruitsappbackend.modulus.product.entity.Product;
import com.hamza.fruitsappbackend.modulus.order.exception.OrderItemNotFoundException;
import com.hamza.fruitsappbackend.modulus.product.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.modulus.order.repository.OrderItemRepository;
import com.hamza.fruitsappbackend.modulus.product.repository.ProductRepository;
import com.hamza.fruitsappbackend.modulus.order.service.OrderItemService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
        Product product = productRepository.findById(orderItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", orderItemDTO.getProductId().toString()));

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(orderItemDTO.getQuantity());
        orderItem.setPrice(BigDecimal.valueOf(product.getPrice()));

        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        logger.debug("OrderItem saved with product ID {} and quantity {}", orderItemDTO.getProductId(), orderItemDTO.getQuantity());

        return mapOrderItemToDTO(savedOrderItem);
    }

    @Override
    @Cacheable(value = "orderItems", key = "#id")
    public Optional<OrderItemDTO> getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .map(this::mapOrderItemToDTO);
    }

    @Override
    @Cacheable(value = "orderItemsByOrderId", key = "#orderId")
    public List<OrderItemDTO> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(this::mapOrderItemToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderItemDTO> getAllOrderItems() {
        return orderItemRepository.findAll().stream()
                .map(this::mapOrderItemToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemDTO updateOrderItem(OrderItemDTO orderItemDTO) {
        OrderItem existingOrderItem = orderItemRepository.findById(orderItemDTO.getId())
                .orElseThrow(() -> new OrderItemNotFoundException("id", orderItemDTO.getId().toString()));

        if (orderItemDTO.getQuantity() > 0) {
            existingOrderItem.setQuantity(orderItemDTO.getQuantity());
        }

        Product product = productRepository.findById(orderItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", orderItemDTO.getProductId().toString()));
        existingOrderItem.setProduct(product);
        BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
        logger.debug("Updating price for OrderItem: {}", productPrice);
        existingOrderItem.setPrice(productPrice);

        OrderItem updatedOrderItem = orderItemRepository.save(existingOrderItem);
        return mapOrderItemToDTO(updatedOrderItem);
    }

    @Override
    public void deleteOrderItemById(Long id) {
        if (!orderItemRepository.existsById(id)) {
            throw new OrderItemNotFoundException("id", id.toString());
        }
        orderItemRepository.deleteById(id);
    }

    private OrderItemDTO mapOrderItemToDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
        orderItemDTO.setProductId(orderItem.getProduct().getId());
        return orderItemDTO;
    }
}
