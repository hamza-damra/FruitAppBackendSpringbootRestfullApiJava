package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.ReviewDTO;
import com.hamza.fruitsappbackend.service.ReviewService;
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
    public ResponseEntity<ReviewDTO> createReview(@RequestHeader("Authorization") String token, @RequestBody ReviewDTO reviewDTO) {
        String jwtToken = token.replace("Bearer ", "");
        ReviewDTO savedReview = reviewService.saveReview(reviewDTO, jwtToken);
        return ResponseEntity.ok(savedReview);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
        ReviewDTO reviewDTO = reviewService.getReviewById(id);
        return ResponseEntity.ok(reviewDTO);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByProductId(@PathVariable Long productId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUserId(@PathVariable Long userId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody ReviewDTO reviewDTO) {
        String jwtToken = token.replace("Bearer ", "");
        reviewDTO.setId(id);
        ReviewDTO updatedReview = reviewService.updateReview(reviewDTO, jwtToken);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReviewById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        reviewService.deleteReviewById(id, jwtToken);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<ReviewDTO> likeReview(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        ReviewDTO reviewDTO = reviewService.likeReview(id, jwtToken);
        return ResponseEntity.ok(reviewDTO);
    }

}
