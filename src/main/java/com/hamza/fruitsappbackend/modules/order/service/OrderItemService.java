package com.hamza.fruitsappbackend.modules.order.service;

import com.hamza.fruitsappbackend.modules.order.dto.OrderItemDTO;
import com.hamza.fruitsappbackend.modules.order.dto.OrderItemsResponseDTO;

import java.util.List;
import java.util.Optional;

public interface OrderItemService {

    OrderItemDTO saveOrderItem(String token, OrderItemDTO orderItemDTO);

    Optional<OrderItemDTO> getOrderItemById(String token, Long id);

    OrderItemsResponseDTO getOrderItemsByOrderId(String token, Long orderId);

    List<OrderItemDTO> getAllOrderItems(String token);

    OrderItemDTO updateOrderItem(String token, OrderItemDTO orderItemDTO);

    void deleteOrderItemById(String token, Long id);
}
