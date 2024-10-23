package com.hamza.fruitsappbackend.modules.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hamza.fruitsappbackend.constant.OrderStatus;
import com.hamza.fruitsappbackend.constant.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;

    private BigDecimal totalPrice;

    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @PastOrPresent(message = "Creation date cannot be in the future")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @PastOrPresent(message = "Updated date cannot be in the future")
    private LocalDateTime updatedAt;

    private Long userId;

    private Long addressId;
}
