package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.OrderDTO;
import com.hamza.fruitsappbackend.entity.Order;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.entity.Address;
import com.hamza.fruitsappbackend.repository.OrderRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.repository.AddressRepository;
import com.hamza.fruitsappbackend.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, AddressRepository addressRepository, ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public OrderDTO saveOrder(OrderDTO orderDTO) {
        Order order = modelMapper.map(orderDTO, Order.class);
        setUserAndAddress(orderDTO, order);
        Order savedOrder = orderRepository.save(order);
        return modelMapper.map(savedOrder, OrderDTO.class);
    }

    @Override
    public Optional<OrderDTO> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(order -> modelMapper.map(order, OrderDTO.class));
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrder(OrderDTO orderDTO) {
        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderDTO.getId()));
        mapOrderDetails(orderDTO, order);
        setUserAndAddress(orderDTO, order);
        Order updatedOrder = orderRepository.save(order);
        return modelMapper.map(updatedOrder, OrderDTO.class);
    }

    @Override
    public void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteOrdersByUserId(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            orderRepository.deleteByUser(userOptional.get());
        } else {
            throw new RuntimeException("User not found");
        }
    }

    @Override
    @Transactional
    public void deleteOrderByIdAndUserId(Long orderId, Long userId) {
        Optional<Order> orderOptional = orderRepository.findByIdAndUserId(orderId, userId);
        if (orderOptional.isPresent()) {
            orderRepository.deleteByIdAndUserId(orderId, userId);
        } else {
            throw new EntityNotFoundException("Order not found for order ID: " + orderId + " and user ID: " + userId);
        }
    }

    @Override
    @Transactional
    public OrderDTO updateOrderByUserIdAndOrderId(Long orderId, Long userId, OrderDTO orderDTO) {
        Optional<Order> existingOrderOpt = orderRepository.findByIdAndUserId(orderId, userId);
        if (existingOrderOpt.isPresent()) {
            Order existingOrder = existingOrderOpt.get();
            mapOrderDetails(orderDTO, existingOrder);
            setUserAndAddress(orderDTO, existingOrder);
            Order updatedOrder = orderRepository.save(existingOrder);
            return modelMapper.map(updatedOrder, OrderDTO.class);
        } else {
            throw new EntityNotFoundException("Order not found for order ID: " + orderId + " and user ID: " + userId);
        }
    }

    // Helper method to map OrderDTO details to an Order entity
    private void mapOrderDetails(OrderDTO orderDTO, Order order) {
        order.setTotalPrice(orderDTO.getTotalPrice());
        order.setStatus(orderDTO.getStatus());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
    }

    // Helper method to set the User and Address in an Order entity
    private void setUserAndAddress(OrderDTO orderDTO, Order order) {
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + orderDTO.getUserId()));
        order.setUser(user);

        Address address = addressRepository.findById(orderDTO.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException("Address not found with ID: " + orderDTO.getAddressId()));
        order.setAddress(address);
    }
}
