package com.hamza.fruitsappbackend.modules.review.controller;

import com.hamza.fruitsappbackend.modules.review.dto.ReviewDTO;
import com.hamza.fruitsappbackend.modules.review.dto.ReviewsResponse;
import com.hamza.fruitsappbackend.modules.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/add")
    public ResponseEntity<ReviewDTO> createReview(@RequestHeader("Authorization") String token, @RequestBody @Valid ReviewDTO reviewDTO) {
        ReviewDTO savedReview = reviewService.addReview(reviewDTO, token);
        return ResponseEntity.ok(savedReview);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
        ReviewDTO reviewDTO = reviewService.getReviewById(id);
        return ResponseEntity.ok(reviewDTO);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ReviewsResponse> getReviewsByProductId(@PathVariable Long productId, @RequestHeader("Authorization") String token) {
        ReviewsResponse reviews = reviewService.getReviewsForProduct(productId, token);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUserId(@PathVariable Long userId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody @Valid ReviewDTO reviewDTO) {
        reviewDTO.setId(id);
        ReviewDTO updatedReview = reviewService.updateReview(reviewDTO, token);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReviewById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        reviewService.deleteReviewById(id, token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/{productId}")
    public ResponseEntity<Void> deleteReviewsByUserIdAndProductId(@RequestHeader("Authorization") String token, @PathVariable Long userId, @PathVariable Long productId) {
        reviewService.deleteReviewsByUserIdAndProductId(userId, productId, token);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<ReviewDTO> likeReview(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        ReviewDTO reviewDTO = reviewService.likeReview(id, token);
        return ResponseEntity.ok(reviewDTO);
    }

}
