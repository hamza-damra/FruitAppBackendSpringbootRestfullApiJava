package com.hamza.fruitsappbackend.modules.review.dto;

import lombok.*;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewsResponse {
    private AllReviewsCustomResponse userReview;
    private List<AllReviewsCustomResponse> otherReviews;
}
