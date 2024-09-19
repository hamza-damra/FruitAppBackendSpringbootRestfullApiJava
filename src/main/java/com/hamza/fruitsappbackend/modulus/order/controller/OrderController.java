package com.hamza.fruitsappbackend.modulus.order.controller;

import com.hamza.fruitsappbackend.modulus.order.dto.OrderDTO;
import com.hamza.fruitsappbackend.modulus.order.service.OrderService;
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
    public ResponseEntity<OrderDTO> createOrder(@RequestHeader("Authorization") String token, @Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO savedOrder = orderService.saveOrder(orderDTO, token);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Optional<OrderDTO> orderDTO = orderService.getOrderById(id, token);
        return orderDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@RequestHeader("Authorization") String token, @PathVariable Long userId) {
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId, token);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders(@RequestHeader("Authorization") String token) {
        List<OrderDTO> orders = orderService.getAllOrders(token);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/user/{userId}")
    public ResponseEntity<OrderDTO> updateOrderByUserIdAndOrderId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId,
            @PathVariable Long userId,
            @Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO updatedOrder = orderService.updateOrderByUserIdAndOrderId(orderId, userId, orderDTO, token);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        orderService.deleteOrderById(id, token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteOrdersByUserId(@RequestHeader("Authorization") String token, @PathVariable Long userId) {
        orderService.deleteOrdersByUserId(userId, token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/user/{userId}")
    public ResponseEntity<Void> deleteOrderByIdAndUserId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId,
            @PathVariable Long userId) {
        orderService.deleteOrderByIdAndUserId(orderId, userId, token);
        return ResponseEntity.noContent().build();
    }
}
