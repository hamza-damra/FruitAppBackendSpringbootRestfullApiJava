package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.OrderDTO;
import com.hamza.fruitsappbackend.entity.Order;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.exception.OrderNotFoundException;
import com.hamza.fruitsappbackend.repository.OrderRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.OrderService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
        return modelMapper.map(savedOrder, OrderDTO.class);
    }

    @Override
    public Optional<OrderDTO> getOrderById(Long id, String token) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("id", id.toString()));

        authorizationUtils.checkUserOrAdminRole(token, order.getUser().getId());

        return Optional.of(modelMapper.map(order, OrderDTO.class));
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId, String token) {
        authorizationUtils.checkUserOrAdminRole(token, userId);
        return orderRepository.findByUserId(userId).stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAllOrders(String token) {
        authorizationUtils.checkAdminRole(token);
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrderByUserIdAndOrderId(Long orderId, Long userId, OrderDTO orderDTO, String token) {
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId.toString()));

        modelMapper.map(orderDTO, existingOrder);
        Order updatedOrder = orderRepository.save(existingOrder);
        return modelMapper.map(updatedOrder, OrderDTO.class);
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
}
