package com.hamza.fruitsappbackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name should be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description should be between 10 and 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private double price;

    @Min(value = 0, message = "Stock quantity must be a non-negative number")
    private int stockQuantity;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotNull(message = "Category ID cannot be null")
    @Min(value = 1, message = "Category ID must be a positive number")
    private Long categoryId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Product weight must be greater than 0")
    private double productWeight;

    @Min(value = 0, message = "Calories must be a non-negative number")
    private int calories;

    private LocalDateTime expirationDate;

    private Double totalRating;
    private int counterFiveStars;
    private int counterFourStars;
    private int counterThreeStars;
    private int counterTwoStars;
    private int counterOneStars;
}
