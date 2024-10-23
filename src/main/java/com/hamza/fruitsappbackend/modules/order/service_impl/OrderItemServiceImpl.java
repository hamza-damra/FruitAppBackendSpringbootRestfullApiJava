package com.hamza.fruitsappbackend.modules.order.service_impl;

import com.hamza.fruitsappbackend.modules.order.dto.OrderItemDTO;
import com.hamza.fruitsappbackend.modules.order.dto.OrderItemsResponseDTO;
import com.hamza.fruitsappbackend.modules.order.entity.Order;
import com.hamza.fruitsappbackend.modules.order.entity.OrderItem;
import com.hamza.fruitsappbackend.modules.order.exception.OrderNotFoundException;
import com.hamza.fruitsappbackend.modules.order.repository.OrderRepository;
import com.hamza.fruitsappbackend.modules.product.entity.Product;
import com.hamza.fruitsappbackend.modules.order.exception.OrderItemNotFoundException;
import com.hamza.fruitsappbackend.modules.product.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.modules.order.repository.OrderItemRepository;
import com.hamza.fruitsappbackend.modules.product.repository.ProductRepository;
import com.hamza.fruitsappbackend.modules.order.service.OrderItemService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    private static final Logger logger = LoggerFactory.getLogger(OrderItemServiceImpl.class);

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AuthorizationUtils authorizationUtils;
    private final ModelMapper modelMapper;

    @Autowired
    public OrderItemServiceImpl(OrderItemRepository orderItemRepository, OrderRepository orderRepository, ProductRepository productRepository, AuthorizationUtils authorizationUtils, ModelMapper modelMapper) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.authorizationUtils = authorizationUtils;
        this.modelMapper = modelMapper;
    }

    @Override
    public OrderItemDTO saveOrderItem(String token, OrderItemDTO orderItemDTO) {
        authorizationUtils.checkUserOrAdminRole(token, authorizationUtils.getUserFromToken(token).getId());
        Product product = productRepository.findById(orderItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", orderItemDTO.getProductId().toString()));

        OrderItem orderItem = modelMapper.map(orderItemDTO, OrderItem.class);
        orderItem.setProduct(product);
        orderItem.setPrice(BigDecimal.valueOf(product.getPrice()));

        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        logger.debug("OrderItem saved with product ID {} and quantity {}", orderItemDTO.getProductId(), orderItemDTO.getQuantity());

        return modelMapper.map(savedOrderItem, OrderItemDTO.class);
    }

    @Override
    @Cacheable(value = "orderItems", key = "#id")
    public Optional<OrderItemDTO> getOrderItemById(String token, Long id) {
        authorizationUtils.checkUserOrAdminRole(token, authorizationUtils.getUserFromToken(token).getId());
        return orderItemRepository.findById(id)
                .map(orderItem -> modelMapper.map(orderItem, OrderItemDTO.class));
    }

    @Override
    public OrderItemsResponseDTO getOrderItemsByOrderId(String token, Long orderId) {
        authorizationUtils.checkUserOrAdminRole(token, authorizationUtils.getUserFromToken(token).getId());
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new OrderNotFoundException("id", orderId.toString())
        );

        List<OrderItemDTO> orderItems = order.getOrderItems()
                .stream()
                .sorted(Comparator.comparing(OrderItem::getCreatedAt).reversed())
                .map(orderItem -> modelMapper.map(orderItem, OrderItemDTO.class))
                .toList();
      logger.info("orderItems retrieved by order ID {}: {}", orderId, orderItems);
        return new OrderItemsResponseDTO(orderItems.size(), orderItems);
    }


    @Override
    public List<OrderItemDTO> getAllOrderItems(String token) {
        authorizationUtils.checkAdminRole(token);

        return orderItemRepository.findAll().stream()
                .map(orderItem -> modelMapper.map(orderItem, OrderItemDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemDTO updateOrderItem(String token, OrderItemDTO orderItemDTO) {
        authorizationUtils.checkUserOrAdminRole(token, authorizationUtils.getUserFromToken(token).getId());

        OrderItem existingOrderItem = orderItemRepository.findById(orderItemDTO.getId())
                .orElseThrow(() -> new OrderItemNotFoundException("id", orderItemDTO.getId().toString()));

        existingOrderItem.setQuantity(orderItemDTO.getQuantity());

        Product product = productRepository.findById(orderItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", orderItemDTO.getProductId().toString()));
        existingOrderItem.setProduct(product);
        existingOrderItem.setPrice(BigDecimal.valueOf(product.getPrice()));

        OrderItem updatedOrderItem = orderItemRepository.save(existingOrderItem);
        return modelMapper.map(updatedOrderItem, OrderItemDTO.class);
    }

    @Override
    public void deleteOrderItemById(String token, Long id) {
        authorizationUtils.checkUserOrAdminRole(token, authorizationUtils.getUserFromToken(token).getId());

        if (!orderItemRepository.existsById(id)) {
            throw new OrderItemNotFoundException("id", id.toString());
        }
        orderItemRepository.deleteById(id);
    }
}
