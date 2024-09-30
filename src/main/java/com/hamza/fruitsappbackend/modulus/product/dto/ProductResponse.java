package com.hamza.fruitsappbackend.modulus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private int itemsPerPage;
    private int currentPage;
    private long totalItems;
    private int totalPages;
    private boolean isLastPage;
    private List<ProductDTO> items;
}
