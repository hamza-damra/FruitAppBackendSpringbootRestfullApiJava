package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.OrderItemDTO;
import com.hamza.fruitsappbackend.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Autowired
    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    // Create a new OrderItem
    @PostMapping
    public ResponseEntity<OrderItemDTO> createOrderItem(@RequestBody OrderItemDTO orderItemDTO) {
        OrderItemDTO savedOrderItem = orderItemService.saveOrderItem(orderItemDTO);
        return new ResponseEntity<>(savedOrderItem, HttpStatus.CREATED);
    }

    // Get an OrderItem by ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderItemDTO> getOrderItemById(@PathVariable Long id) {
        Optional<OrderItemDTO> orderItemDTO = orderItemService.getOrderItemById(id);
        return orderItemDTO.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Get all OrderItems by Order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemDTO>> getOrderItemsByOrderId(@PathVariable Long orderId) {
        List<OrderItemDTO> orderItems = orderItemService.getOrderItemsByOrderId(orderId);
        return new ResponseEntity<>(orderItems, HttpStatus.OK);
    }

    // Get all OrderItems
    @GetMapping
    public ResponseEntity<List<OrderItemDTO>> getAllOrderItems() {
        List<OrderItemDTO> orderItems = orderItemService.getAllOrderItems();
        return new ResponseEntity<>(orderItems, HttpStatus.OK);
    }

    // Update an OrderItem
    @PutMapping("/{id}")
    public ResponseEntity<OrderItemDTO> updateOrderItem(@PathVariable Long id, @RequestBody OrderItemDTO orderItemDTO) {
        orderItemDTO.setId(id);  // Ensure the ID in the DTO matches the path variable
        OrderItemDTO updatedOrderItem = orderItemService.updateOrderItem(orderItemDTO);
        return updatedOrderItem != null ? new ResponseEntity<>(updatedOrderItem, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete an OrderItem by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItemById(@PathVariable Long id) {
        orderItemService.deleteOrderItemById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
