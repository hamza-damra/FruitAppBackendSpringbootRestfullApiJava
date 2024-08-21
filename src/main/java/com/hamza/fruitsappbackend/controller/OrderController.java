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
    public ResponseEntity<OrderDTO> createOrder(@RequestHeader("Authorization") String token, @Valid @RequestBody OrderDTO orderDTO) {
        String jwtToken = token.replace("Bearer ", "");
        OrderDTO savedOrder = orderService.saveOrder(orderDTO, jwtToken);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        Optional<OrderDTO> orderDTO = orderService.getOrderById(id, jwtToken);
        return orderDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@RequestHeader("Authorization") String token, @PathVariable Long userId) {
        String jwtToken = token.replace("Bearer ", "");
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId, jwtToken);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        List<OrderDTO> orders = orderService.getAllOrders(jwtToken);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/user/{userId}")
    public ResponseEntity<OrderDTO> updateOrderByUserIdAndOrderId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId,
            @PathVariable Long userId,
            @Valid @RequestBody OrderDTO orderDTO) {
        String jwtToken = token.replace("Bearer ", "");
        OrderDTO updatedOrder = orderService.updateOrderByUserIdAndOrderId(orderId, userId, orderDTO, jwtToken);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        orderService.deleteOrderById(id, jwtToken);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteOrdersByUserId(@RequestHeader("Authorization") String token, @PathVariable Long userId) {
        String jwtToken = token.replace("Bearer ", "");
        orderService.deleteOrdersByUserId(userId, jwtToken);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/user/{userId}")
    public ResponseEntity<Void> deleteOrderByIdAndUserId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId,
            @PathVariable Long userId) {
        String jwtToken = token.replace("Bearer ", "");
        orderService.deleteOrderByIdAndUserId(orderId, userId, jwtToken);
        return ResponseEntity.noContent().build();
    }
}
