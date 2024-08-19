package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.OrderDTO;
import com.hamza.fruitsappbackend.dto.OrderItemDTO;
import com.hamza.fruitsappbackend.entity.Order;
import com.hamza.fruitsappbackend.entity.OrderItem;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.entity.Address;
import com.hamza.fruitsappbackend.exception.OrderNotFoundException;
import com.hamza.fruitsappbackend.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.exception.AddressNotFoundException;
import com.hamza.fruitsappbackend.repository.*;
import com.hamza.fruitsappbackend.service.OrderService;
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
        return mapOrderToDTO(savedOrder, savedOrderItems);
    }

    @Override
    public OrderDTO updateOrder(OrderDTO orderDTO) {
        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new OrderNotFoundException("id", orderDTO.getId().toString(), "userId", orderDTO.getUserId().toString()));
        setUserAndAddress(orderDTO, order);
        order.getOrderItems().clear();
        List<OrderItemDTO> savedOrderItems = saveOrderItems(orderDTO, order);
        Order updatedOrder = orderRepository.save(order);
        return mapOrderToDTO(updatedOrder, savedOrderItems);
    }

    private List<OrderItemDTO> saveOrderItems(OrderDTO orderDTO, Order order) {
        return orderDTO.getOrderItems().stream()
                .map(orderItemDTO -> saveOrderItem(orderItemDTO, order))
                .collect(Collectors.toList());
    }

    private OrderItemDTO saveOrderItem(OrderItemDTO orderItemDTO, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(productRepository.findById(orderItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("id", orderItemDTO.getProductId().toString())));
        orderItem.setQuantity(orderItemDTO.getQuantity());
        orderItem.setPrice(BigDecimal.valueOf(orderItem.getProduct().getPrice()));
        return modelMapper.map(orderItemRepository.save(orderItem), OrderItemDTO.class);
    }

    private void setUserAndAddress(OrderDTO orderDTO, Order order) {
        order.setUser(findUserById(orderDTO.getUserId()));
        order.setAddress(findAddressById(orderDTO.getAddressId()));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("id", userId.toString()));
    }

    private Address findAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("id", addressId.toString()));
    }

    private OrderDTO mapOrderToDTO(Order order, List<OrderItemDTO> savedOrderItems) {
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        orderDTO.setOrderItems(savedOrderItems);
        return orderDTO;
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
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("id", id.toString());
        }
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteOrdersByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("id", userId.toString()));
        orderRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    public void deleteOrderByIdAndUserId(Long orderId, Long userId) {
        orderRepository.findByIdAndUserId(orderId, userId)
                .ifPresentOrElse(order -> orderRepository.deleteByIdAndUserId(orderId, userId),
                        () -> { throw new OrderNotFoundException("orderId", orderId.toString(), "userId", userId.toString()); });
    }

    @Override
    @Transactional
    public OrderDTO updateOrderByUserIdAndOrderId(Long orderId, Long userId, OrderDTO orderDTO) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(existingOrder -> {
                    setUserAndAddress(orderDTO, existingOrder);
                    existingOrder.getOrderItems().clear();
                    List<OrderItemDTO> savedOrderItems = saveOrderItems(orderDTO, existingOrder);
                    Order updatedOrder = orderRepository.save(existingOrder);
                    return mapOrderToDTO(updatedOrder, savedOrderItems);
                })
                .orElseThrow(() -> new OrderNotFoundException("orderId", orderId.toString(), "userId", userId.toString()));
    }
}
