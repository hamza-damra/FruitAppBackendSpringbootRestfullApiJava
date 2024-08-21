package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.ReviewDTO;
import com.hamza.fruitsappbackend.dto.ReviewImageDto;
import com.hamza.fruitsappbackend.entity.Review;
import com.hamza.fruitsappbackend.entity.ReviewImage;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.entity.UserReviewLike;
import com.hamza.fruitsappbackend.exception.ReviewNotFoundException;
import com.hamza.fruitsappbackend.repository.ReviewRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.repository.UserReviewLikeRepository;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import com.hamza.fruitsappbackend.service.ReviewService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserReviewLikeRepository userReviewLikeRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, UserRepository userRepository,
                             ModelMapper modelMapper, JwtTokenProvider jwtTokenProvider, UserReviewLikeRepository userReviewLikeRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userReviewLikeRepository = userReviewLikeRepository;
    }

    private void checkUserRole(User user) {
        if (user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_USER") || role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }
    }

    @Override
    public ReviewDTO saveReview(ReviewDTO reviewDTO, String token) {
        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        checkUserRole(user);

        Review review = modelMapper.map(reviewDTO, Review.class);
        review.setUser(user);

        List<ReviewImage> reviewImages = mapImageDtosToReviewImages(reviewDTO.getImageDtos(), review);
        review.setReviewImages(reviewImages);

        Review savedReview = reviewRepository.save(review);

        ReviewDTO savedReviewDTO = modelMapper.map(savedReview, ReviewDTO.class);

        List<ReviewImageDto> imageDtos = savedReview.getReviewImages().stream()
                .map(image -> modelMapper.map(image, ReviewImageDto.class))
                .collect(Collectors.toList());
        savedReviewDTO.setImageDtos(imageDtos);

        return savedReviewDTO;
    }

    @Override
    public ReviewDTO updateReview(ReviewDTO reviewDTO, String token) {
        Review existingReview = reviewRepository.findById(reviewDTO.getId())
                .orElseThrow(() -> new ReviewNotFoundException("id", reviewDTO.getId().toString()));

        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        checkUserRole(user);

        if (!existingReview.getUser().equals(user) && user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to update this review");
        }

        existingReview.setRating(reviewDTO.getRating());
        existingReview.setComment(reviewDTO.getComment());

        List<ReviewImage> existingImages = existingReview.getReviewImages();
        List<ReviewImage> updatedImages = mapImageDtosToReviewImages(reviewDTO.getImageDtos(), existingReview);

        existingImages.removeIf(image -> !updatedImages.contains(image));

        for (ReviewImage updatedImage : updatedImages) {
            if (!existingImages.contains(updatedImage)) {
                existingImages.add(updatedImage);
            }
        }

        existingReview.setReviewImages(existingImages);

        Review updatedReview = reviewRepository.save(existingReview);

        List<ReviewImageDto> imageDtos = updatedReview.getReviewImages().stream()
                .map(image -> modelMapper.map(image, ReviewImageDto.class))
                .collect(Collectors.toList());

        ReviewDTO updatedReviewDTO = modelMapper.map(updatedReview, ReviewDTO.class);
        updatedReviewDTO.setImageDtos(imageDtos);
        return updatedReviewDTO;
    }

    @Override
    public ReviewDTO getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(review -> modelMapper.map(review, ReviewDTO.class))
                .orElseThrow(() -> new ReviewNotFoundException("id", id.toString()));
    }

    @Override
    public List<ReviewDTO> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(review -> modelMapper.map(review, ReviewDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(review -> modelMapper.map(review, ReviewDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(review -> modelMapper.map(review, ReviewDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReviewById(Long id, String token) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("id", id.toString()));

        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        checkUserRole(user);

        if (!review.getUser().equals(user) && user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to delete this review");
        }

        reviewRepository.deleteById(id);
    }

    @Override
    public ReviewDTO likeReview(Long reviewId, String token) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("id", reviewId.toString()));

        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        checkUserRole(user);

        // Check if the user has already liked this review
        userReviewLikeRepository.findByUserIdAndReviewId(user.getId(), reviewId)
                .ifPresent(like -> {
                    throw new IllegalStateException("User has already liked this review");
                });

        // Create a new like entry
        UserReviewLike like = new UserReviewLike();
        like.setUser(user);
        like.setReview(review);
        userReviewLikeRepository.save(like);

        // Increment the like count for the review
        review.setLikeCount(review.getLikeCount() + 1);
        Review updatedReview = reviewRepository.save(review);

        return modelMapper.map(updatedReview, ReviewDTO.class);
    }

    private List<ReviewImage> mapImageDtosToReviewImages(List<ReviewImageDto> imageDtos, Review review) {
        if (imageDtos == null || imageDtos.isEmpty()) {
            return List.of();
        }
        return imageDtos.stream()
                .map(dto -> new ReviewImage(null, dto.getImageUrl(), review))
                .collect(Collectors.toList());
    }
}
