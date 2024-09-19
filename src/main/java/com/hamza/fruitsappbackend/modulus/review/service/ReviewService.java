package com.hamza.fruitsappbackend.modulus.review.service;

import com.hamza.fruitsappbackend.modulus.review.dto.ReviewDTO;

import java.util.List;

public interface ReviewService {
    ReviewDTO saveReview(ReviewDTO reviewDTO, String token);
    ReviewDTO updateReview(ReviewDTO reviewDTO, String token);
    ReviewDTO getReviewById(Long id);
    List<ReviewDTO> getReviewsByProductId(Long productId);
    List<ReviewDTO> getReviewsByUserId(Long userId);
    List<ReviewDTO> getAllReviews();
    void deleteReviewById(Long id, String token);
    ReviewDTO likeReview(Long reviewId, String token);

    void deleteReviewsByUserIdAndProductId(Long userId, Long productId, String token);
}
