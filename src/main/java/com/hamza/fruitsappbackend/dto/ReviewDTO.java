package com.hamza.fruitsappbackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hamza.fruitsappbackend.validators.annotation.DoubleMax;
import com.hamza.fruitsappbackend.validators.annotation.DoubleMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ReviewDTO {

    private Long id;

    @DoubleMin(value = 1.0, message = "Rating must be at least 1")
    @DoubleMax(value = 5.0, message = "Rating must be at most 5")
    private double rating;

    private int likeCount;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<ReviewImageDto> imageDtos;

    @NotBlank(message = "Comment is required")
    private String comment;
}
