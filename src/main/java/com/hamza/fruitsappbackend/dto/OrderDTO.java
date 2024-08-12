package com.hamza.fruitsappbackend.dto;

import com.hamza.fruitsappbackend.constant.OrderStatus;
import com.hamza.fruitsappbackend.constant.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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

    @NotNull(message = "Order ID cannot be null")
    private Long id;

    @Positive(message = "Total price must be a positive value")
    private double totalPrice;

    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod;

    @PastOrPresent(message = "Creation date cannot be in the future")
    private LocalDateTime createdAt;

    @PastOrPresent(message = "Updated date cannot be in the future")
    private LocalDateTime updatedAt;

    @NotNull(message = "User information is required")
    @Valid
    private UserDTO user;

    @NotNull(message = "Address information is required")
    @Valid
    private AddressDTO address;

    @NotEmpty(message = "Order must contain at least one order item")
    @Valid
    private List<OrderItemDTO> orderItems;
}
