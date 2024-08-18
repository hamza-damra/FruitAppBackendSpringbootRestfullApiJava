package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.OrderDTO;
import com.hamza.fruitsappbackend.dto.OrderItemDTO;
import com.hamza.fruitsappbackend.entity.Order;
import com.hamza.fruitsappbackend.entity.OrderItem;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.entity.Address;
import com.hamza.fruitsappbackend.repository.*;
import com.hamza.fruitsappbackend.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, OrderItemRepository orderItemRepository,
                            AddressRepository addressRepository, ProductRepository productRepository,
                            ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public OrderDTO saveOrder(OrderDTO orderDTO) {

        Order order = modelMapper.map(orderDTO, Order.class);

        setUserAndAddress(orderDTO, order);

        Order savedOrder = orderRepository.save(order);

        List<OrderItemDTO> savedOrderItems = saveOrderItems(orderDTO, savedOrder);

        OrderDTO savedOrderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        savedOrderDTO.setOrderItems(savedOrderItems);

        return savedOrderDTO;
    }

    @Override
    public OrderDTO updateOrder(OrderDTO orderDTO) {
        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderDTO.getId()));

        setUserAndAddress(orderDTO, order);

        order.getOrderItems().clear();

        List<OrderItemDTO> savedOrderItems = saveOrderItems(orderDTO, order);

        Order updatedOrder = orderRepository.save(order);

        OrderDTO updatedOrderDTO = modelMapper.map(updatedOrder, OrderDTO.class);
        updatedOrderDTO.setOrderItems(savedOrderItems);

        return updatedOrderDTO;
    }

    private List<OrderItemDTO> saveOrderItems(OrderDTO orderDTO, Order order) {
        return orderDTO.getOrderItems().stream()
                .map(orderItemDTO -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(productRepository.findById(orderItemDTO.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + orderItemDTO.getProductId())));
                    orderItem.setQuantity(orderItemDTO.getQuantity());
                    orderItem.setPrice(BigDecimal.valueOf(orderItem.getProduct().getPrice()));

                    OrderItem savedOrderItem = orderItemRepository.save(orderItem);
                    return modelMapper.map(savedOrderItem, OrderItemDTO.class);
                })
                .collect(Collectors.toList());
    }

    private void setUserAndAddress(OrderDTO orderDTO, Order order) {
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + orderDTO.getUserId()));
        order.setUser(user);

        Address address = addressRepository.findById(orderDTO.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException("Address not found with ID: " + orderDTO.getAddressId()));
        order.setAddress(address);
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
            setUserAndAddress(orderDTO, existingOrder);

            existingOrder.getOrderItems().clear();
            List<OrderItemDTO> savedOrderItems = saveOrderItems(orderDTO, existingOrder);

            Order updatedOrder = orderRepository.save(existingOrder);
            OrderDTO updatedOrderDTO = modelMapper.map(updatedOrder, OrderDTO.class);
            updatedOrderDTO.setOrderItems(savedOrderItems);

            return updatedOrderDTO;
        } else {
            throw new EntityNotFoundException("Order not found for order ID: " + orderId + " and user ID: " + userId);
        }
    }
}
