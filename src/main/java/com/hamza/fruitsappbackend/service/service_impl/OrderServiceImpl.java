package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.OrderDTO;
import com.hamza.fruitsappbackend.entity.Order;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.exception.OrderNotFoundException;
import com.hamza.fruitsappbackend.repository.OrderRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import com.hamza.fruitsappbackend.service.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository,
                            ModelMapper modelMapper, JwtTokenProvider jwtTokenProvider) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private void checkUserOrAdminRole(String token, Long userId) {
        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (!user.getId().equals(userId) && user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }
    }

    private void checkAdminRole(String token) {
        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }
    }

    @Override
    public OrderDTO saveOrder(OrderDTO orderDTO, String token) {
        checkUserOrAdminRole(token, orderDTO.getUserId());

        Order order = modelMapper.map(orderDTO, Order.class);
        Order savedOrder = orderRepository.save(order);
        return modelMapper.map(savedOrder, OrderDTO.class);
    }

    @Override
    public Optional<OrderDTO> getOrderById(Long id, String token) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("id", id.toString()));

        checkUserOrAdminRole(token, order.getUser().getId());

        return Optional.of(modelMapper.map(order, OrderDTO.class));
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId, String token) {
        checkUserOrAdminRole(token, userId);

        return orderRepository.findByUserId(userId).stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAllOrders(String token) {
        checkAdminRole(token);

        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrderByUserIdAndOrderId(Long orderId, Long userId, OrderDTO orderDTO, String token) {
        checkUserOrAdminRole(token, userId);

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

        checkUserOrAdminRole(token, order.getUser().getId());

        orderRepository.deleteById(id);
    }

    @Override
    public void deleteOrdersByUserId(Long userId, String token) {
        checkAdminRole(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderNotFoundException("User", userId.toString()));

        orderRepository.deleteByUser(user);
    }

    @Override
    public void deleteOrderByIdAndUserId(Long orderId, Long userId, String token) {
        checkUserOrAdminRole(token, userId);

        if (orderRepository.existsByIdAndUserId(orderId, userId)) {
            orderRepository.deleteByIdAndUserId(orderId, userId);
        } else {
            throw new OrderNotFoundException("Order", orderId.toString());
        }
    }
}
