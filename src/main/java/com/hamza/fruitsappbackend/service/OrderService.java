package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.OrderDTO;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderDTO saveOrder(OrderDTO orderDTO);

    Optional<OrderDTO> getOrderById(Long id);

    List<OrderDTO> getOrdersByUserId(Long userId);

    List<OrderDTO> getAllOrders();

    OrderDTO updateOrder(OrderDTO orderDTO);

    void deleteOrderById(Long id);
}
