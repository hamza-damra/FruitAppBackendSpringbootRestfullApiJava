package com.hamza.fruitsappbackend.modules.review.service;

import com.hamza.fruitsappbackend.modules.review.dto.ReviewDTO;
import com.hamza.fruitsappbackend.modules.review.dto.ReviewsResponse;

import java.util.List;

public interface ReviewService {
    ReviewDTO addReview(ReviewDTO reviewDTO, String token);
    ReviewDTO updateReview(ReviewDTO reviewDTO, String token);
    ReviewDTO getReviewById(Long id);
    List<ReviewDTO> getReviewsByProductId(Long productId);
    List<ReviewDTO> getReviewsByUserId(Long userId);
    List<ReviewDTO> getAllReviews();
    void deleteReviewById(Long id, String token);
    ReviewDTO likeReview(Long reviewId, String token);
    ReviewsResponse getReviewsForProduct(Long productId, String token);

    void deleteReviewsByUserIdAndProductId(Long userId, Long productId, String token);
}
