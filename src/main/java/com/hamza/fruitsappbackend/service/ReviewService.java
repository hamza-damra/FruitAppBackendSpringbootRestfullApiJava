package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.ReviewDTO;

import java.util.List;
import java.util.Optional;

public interface ReviewService {

    ReviewDTO saveReview(ReviewDTO reviewDTO);

    Optional<ReviewDTO> getReviewById(Long id);

    List<ReviewDTO> getReviewsByProductId(Long productId);

    List<ReviewDTO> getReviewsByUserId(Long userId);

    List<ReviewDTO> getAllReviews();

    ReviewDTO updateReview(ReviewDTO reviewDTO);

    void deleteReviewById(Long id);
}
