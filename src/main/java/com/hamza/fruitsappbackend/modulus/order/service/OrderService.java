package com.hamza.fruitsappbackend.modulus.order.service;

import com.hamza.fruitsappbackend.constant.OrderStatus;
import com.hamza.fruitsappbackend.modulus.order.dto.OrderDTO;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderDTO saveOrder(OrderDTO orderDTO, String token);
    Optional<OrderDTO> getOrderById(Long id, String token);

    List<OrderDTO> getOrdersByUserId(Long userId, String token);

    List<OrderDTO> getAllOrders(String token);

    OrderDTO updateOrderByUserIdAndOrderId(Long orderId, Long userId, OrderDTO orderDTO, String token); // User or Admin

    void deleteOrderById(Long id, String token);

    OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus, String token);

    void deleteOrdersByUserId(Long userId, String token);

    void deleteOrderByIdAndUserId(Long orderId, Long userId, String token);
}
