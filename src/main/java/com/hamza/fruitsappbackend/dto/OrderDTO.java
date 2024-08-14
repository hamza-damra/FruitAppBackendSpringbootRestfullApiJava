package com.hamza.fruitsappbackend.dto;

import com.hamza.fruitsappbackend.constant.OrderStatus;
import com.hamza.fruitsappbackend.constant.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;

    @Positive(message = "Total price must be a positive value")
    @NotNull(message = "Total price is required")
    private Double totalPrice;

    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod;

    @PastOrPresent(message = "Creation date cannot be in the future")
    private LocalDateTime createdAt;

    @PastOrPresent(message = "Updated date cannot be in the future")
    private LocalDateTime updatedAt;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Address ID is required")
    private Long addressId;

    @NotNull(message = "Order items are required")
    private List<OrderItemDTO> orderItems;
}
