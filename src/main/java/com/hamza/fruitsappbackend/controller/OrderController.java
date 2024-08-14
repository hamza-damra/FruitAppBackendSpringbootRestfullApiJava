package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.OrderDTO;
import com.hamza.fruitsappbackend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO savedOrder = orderService.saveOrder(orderDTO);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        Optional<OrderDTO> orderDTO = orderService.getOrderById(id);
        return orderDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/user/{userId}")
    public ResponseEntity<OrderDTO> updateOrderByUserIdAndOrderId(
            @PathVariable Long orderId,
            @PathVariable Long userId,
            @Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO updatedOrder = orderService.updateOrderByUserIdAndOrderId(orderId, userId, orderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteOrdersByUserId(@PathVariable Long userId) {
        orderService.deleteOrdersByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/user/{userId}")
    public ResponseEntity<Void> deleteOrderByIdAndUserId(
            @PathVariable Long orderId,
            @PathVariable Long userId) {
        orderService.deleteOrderByIdAndUserId(orderId, userId);
        return ResponseEntity.noContent().build();
    }
}
