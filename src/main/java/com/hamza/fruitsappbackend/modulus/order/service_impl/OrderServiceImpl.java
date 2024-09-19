package com.hamza.fruitsappbackend.modulus.order.service_impl;

import com.hamza.fruitsappbackend.modulus.order.dto.OrderDTO;
import com.hamza.fruitsappbackend.modulus.order.entity.Order;
import com.hamza.fruitsappbackend.modulus.user.entity.User;
import com.hamza.fruitsappbackend.modulus.order.exception.OrderNotFoundException;
import com.hamza.fruitsappbackend.modulus.order.repository.OrderRepository;
import com.hamza.fruitsappbackend.modulus.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modulus.order.service.OrderService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository,
                            ModelMapper modelMapper, AuthorizationUtils authorizationUtils) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    public OrderDTO saveOrder(OrderDTO orderDTO, String token) {
        authorizationUtils.checkUserOrAdminRole(token, orderDTO.getUserId());
        Order order = modelMapper.map(orderDTO, Order.class);
        Order savedOrder = orderRepository.save(order);
        return mapOrderToDTO(savedOrder);
    }

    @Override
    @Cacheable(value = "orders", key = "#id")
    public Optional<OrderDTO> getOrderById(Long id, String token) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("id", id.toString()));

        authorizationUtils.checkUserOrAdminRole(token, order.getUser().getId());
        return Optional.of(mapOrderToDTO(order));
    }

    @Override
    @Cacheable(value = "ordersByUserId", key = "#userId")
    public List<OrderDTO> getOrdersByUserId(Long userId, String token) {
        authorizationUtils.checkUserOrAdminRole(token, userId);
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "allOrders")
    public List<OrderDTO> getAllOrders(String token) {
        authorizationUtils.checkAdminRole(token);
        return orderRepository.findAll().stream()
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrderByUserIdAndOrderId(Long orderId, Long userId, OrderDTO orderDTO, String token) {
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId.toString()));

        modelMapper.map(orderDTO, existingOrder);
        Order updatedOrder = orderRepository.save(existingOrder);
        return mapOrderToDTO(updatedOrder);
    }

    @Override
    public void deleteOrderById(Long id, String token) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("id", id.toString()));

        authorizationUtils.checkUserOrAdminRole(token, order.getUser().getId());

        orderRepository.deleteById(id);
    }

    @Override
    public void deleteOrdersByUserId(Long userId, String token) {
        authorizationUtils.checkAdminRole(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderNotFoundException("User", userId.toString()));

        orderRepository.deleteByUser(user);
    }

    @Override
    public void deleteOrderByIdAndUserId(Long orderId, Long userId, String token) {
        authorizationUtils.checkUserOrAdminRole(token, userId);

        if (orderRepository.existsByIdAndUserId(orderId, userId)) {
            orderRepository.deleteByIdAndUserId(orderId, userId);
        } else {
            throw new OrderNotFoundException("Order", orderId.toString());
        }
    }

    private OrderDTO mapOrderToDTO(Order order) {
        return modelMapper.map(order, OrderDTO.class);
    }
}
