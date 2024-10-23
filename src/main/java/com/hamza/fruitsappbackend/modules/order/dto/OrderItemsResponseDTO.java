package com.hamza.fruitsappbackend.modules.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemsResponseDTO {

    private Integer totalItems;

    private List<OrderItemDTO> items;
}
