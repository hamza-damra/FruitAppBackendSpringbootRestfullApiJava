package com.hamza.fruitsappbackend.modulus.review.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hamza.fruitsappbackend.modulus.review.entity.ReviewImage;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllReviewsCustomResponse {
    private String username;
    private String userImage;
    private String message;
    private Integer totalLikes;
    private Integer rating;
    private List<ReviewImage> reviewImages;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;

}
