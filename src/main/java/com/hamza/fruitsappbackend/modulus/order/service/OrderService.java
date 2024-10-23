package com.hamza.fruitsappbackend.modulus.order.service;

import com.hamza.fruitsappbackend.constant.OrderStatus;
import com.hamza.fruitsappbackend.modulus.order.dto.OrderDTO;
import com.hamza.fruitsappbackend.modulus.order.dto.OrderResponseDto;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderDTO createOrder(OrderDTO orderDTO, String token);

    Optional<OrderDTO> getOrderById(Long id, String token);

    OrderResponseDto getOrdersByUserId(String token);

    List<OrderDTO> getAllOrders(String token);

    OrderDTO updateOrderByUserTokenAndOrderId(Long orderId, OrderDTO orderDTO, String token);

    void deleteOrderById(Long id, String token);

    OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus, String token);

    void deleteOrdersByUserToken(String token);

    void deleteOrderByIdAndUserToken(Long orderId, String token);
}
