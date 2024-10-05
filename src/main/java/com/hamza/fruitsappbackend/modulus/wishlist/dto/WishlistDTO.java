package com.hamza.fruitsappbackend.modulus.wishlist.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hamza.fruitsappbackend.validation.markers.OnCreate;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishlistDTO {
    private Long id;

    private Long productId;

    private String name;


    private String description;

    private Double price;

    private Integer quantityInCart;

    private Integer stockQuantity;

    private String imageUrl;

    private Long categoryId;

    private Double productWeight;

    private Integer caloriesPer100Grams;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime addedAt;


    private boolean isFavorite;
    private boolean isInCart;
    private Long orderCount = 0L;
    private Integer likeCount = 0;
    private Double totalRating = 0.0;
    private Integer counterFiveStars = 0;
    private Integer counterFourStars = 0;
    private Integer counterThreeStars = 0;
    private Integer counterTwoStars = 0;
    private Integer counterOneStars = 0;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}