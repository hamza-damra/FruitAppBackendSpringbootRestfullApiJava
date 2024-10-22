package com.hamza.fruitsappbackend.modulus.order.dto;

import com.hamza.fruitsappbackend.modulus.order.entity.Order;
import com.hamza.fruitsappbackend.modulus.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class OrderResponseDto {

    private BigDecimal totalPrice = BigDecimal.valueOf(0.0);

    private Integer ordersCount = 0;

    private List<OrderDTO> orders;

}

