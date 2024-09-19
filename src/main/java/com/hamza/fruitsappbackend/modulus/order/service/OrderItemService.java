package com.hamza.fruitsappbackend.modulus.order.service;

import com.hamza.fruitsappbackend.modulus.order.dto.OrderItemDTO;

import java.util.List;
import java.util.Optional;

public interface OrderItemService {

    OrderItemDTO saveOrderItem(OrderItemDTO orderItemDTO);

    Optional<OrderItemDTO> getOrderItemById(Long id);

    List<OrderItemDTO> getOrderItemsByOrderId(Long orderId);

    List<OrderItemDTO> getAllOrderItems();

    OrderItemDTO updateOrderItem(OrderItemDTO orderItemDTO);

    void deleteOrderItemById(Long id);
}
