package com.hamza.fruitsappbackend.modules.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    private BigDecimal totalPrice = BigDecimal.valueOf(0.0);

    private Integer totalOrders = 0;

    private List<OrderDTO> orders = new ArrayList<>();
}
