package com.hamza.fruitsappbackend.modulus.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hamza.fruitsappbackend.utils.ProductDTOSerializer;
import com.hamza.fruitsappbackend.validation.markers.OnCreate;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = ProductDTOSerializer.class)
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Name is required", groups = OnCreate.class)
    @Size(min = 2, max = 100, message = "Name should be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required", groups = OnCreate.class)
    @Size(min = 10, max = 1000, message = "Description should be between 10 and 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    private Integer quantityInCart;

    @Min(value = 0, message = "Stock quantity must be a non-negative number")
    private Integer stockQuantity;

    @NotBlank(message = "Image URL is required", groups = OnCreate.class)
    private String imageUrl;

    @NotNull(message = "Category ID cannot be null")
    @Min(value = 1, message = "Category ID must be a positive number")
    private Long categoryId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Product weight must be greater than 0")
    private Double productWeight;

    @Min(value = 0, message = "caloriesPer100Grams must be a non-negative number")
    private Integer caloriesPer100Grams;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @NotNull(message = "Expiration date must not be null", groups = OnCreate.class)
    private LocalDate expirationDate;


    private boolean isFavorite;
    private boolean isInCart;

    private Integer likeCount = 0;
    private Double totalRating = 0.0;
    private Integer counterFiveStars = 0;
    private Integer counterFourStars = 0;
    private Integer counterThreeStars = 0;
    private Integer counterTwoStars = 0;
    private Integer counterOneStars = 0;


    public void setIsInCart(boolean isInCart) {
        this.isInCart = isInCart;
        this.likeCount = isInCart ? likeCount + 1 : Math.max(likeCount - 1, 0);
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
        this.likeCount = isFavorite ? likeCount + 1 : Math.max(likeCount - 1, 0);
    }
}

