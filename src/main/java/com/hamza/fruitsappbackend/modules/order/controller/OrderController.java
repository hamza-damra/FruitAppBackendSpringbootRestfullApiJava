package com.hamza.fruitsappbackend.modules.order.controller;

import com.hamza.fruitsappbackend.constant.OrderStatus;
import com.hamza.fruitsappbackend.modules.order.dto.OrderDTO;
import com.hamza.fruitsappbackend.modules.order.dto.OrderItemDTO;
import com.hamza.fruitsappbackend.modules.order.dto.OrderItemsResponseDTO;
import com.hamza.fruitsappbackend.modules.order.dto.OrderResponseDto;
import com.hamza.fruitsappbackend.modules.order.service.OrderService;
import com.hamza.fruitsappbackend.modules.order.service.OrderItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @Autowired
    public OrderController(OrderService orderService, OrderItemService orderItemService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestHeader("Authorization") String token, @Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO savedOrder = orderService.createOrder(orderDTO, token);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Optional<OrderDTO> orderDTO = orderService.getOrderById(id, token);
        return orderDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user")
    public ResponseEntity<OrderResponseDto> getOrdersByUserToken(@RequestHeader("Authorization") String token) {
        OrderResponseDto orderResponse = orderService.getOrdersByUserId(token);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders(@RequestHeader("Authorization") String token) {
        List<OrderDTO> orders = orderService.getAllOrders(token);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/user/{orderId}")
    public ResponseEntity<OrderDTO> updateOrderByUserTokenAndOrderToken(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId,
            @Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO updatedOrder = orderService.updateOrderByUserTokenAndOrderId(orderId, orderDTO, token);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        orderService.deleteOrderById(id, token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteOrdersByUserToken(@RequestHeader("Authorization") String token) {
        orderService.deleteOrdersByUserToken(token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{orderId}")
    public ResponseEntity<Void> deleteOrderByIdAndUserToken(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId) {
        orderService.deleteOrderByIdAndUserToken(orderId, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderItemDTO> createOrderItem(@RequestHeader("Authorization") String token, @Valid @RequestBody OrderItemDTO orderItemDTO) {
        OrderItemDTO savedOrderItem = orderItemService.saveOrderItem(token, orderItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrderItem);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<OrderItemDTO> getOrderItemById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Optional<OrderItemDTO> orderItemDTO = orderItemService.getOrderItemById(token,id);
        return orderItemDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/items/order/{orderId}")
    public ResponseEntity<OrderItemsResponseDTO> getOrderItemsByOrderId(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        OrderItemsResponseDTO orderItemsResponseDTO = orderItemService.getOrderItemsByOrderId(token, orderId);
        return ResponseEntity.ok(orderItemsResponseDTO);
    }

    @GetMapping("/items")
    public ResponseEntity<List<OrderItemDTO>> getAllOrderItems(@RequestHeader("Authorization") String token) {
        List<OrderItemDTO> orderItems = orderItemService.getAllOrderItems(token);
        return ResponseEntity.ok(orderItems);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<OrderItemDTO> updateOrderItem(@RequestHeader("Authorization") String token, @PathVariable Long id, @Valid @RequestBody OrderItemDTO orderItemDTO) {
        orderItemDTO.setId(id);
        OrderItemDTO updatedOrderItem = orderItemService.updateOrderItem(token, orderItemDTO);
        return ResponseEntity.ok(updatedOrderItem);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId,
            @RequestParam("newStatus") OrderStatus newStatus) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, newStatus, token);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteOrderItemById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        orderItemService.deleteOrderItemById(token, id);
        return ResponseEntity.noContent().build();
    }
}
