package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.ReviewDTO;
import com.hamza.fruitsappbackend.dto.ReviewImageDto;
import com.hamza.fruitsappbackend.entity.*;
import com.hamza.fruitsappbackend.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.exception.ReviewNotFoundException;
import com.hamza.fruitsappbackend.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.repository.ReviewRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.repository.UserReviewLikeRepository;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import com.hamza.fruitsappbackend.service.ProductService;
import com.hamza.fruitsappbackend.service.ReviewService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import jakarta.transaction.Transactional;
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
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final AuthorizationUtils authorizationUtils;
    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, UserRepository userRepository,
                             ModelMapper modelMapper, JwtTokenProvider jwtTokenProvider,
                             UserReviewLikeRepository userReviewLikeRepository, ProductService productService, ProductRepository productRepository, AuthorizationUtils authorizationUtils) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userReviewLikeRepository = userReviewLikeRepository;
        this.productService = productService;
        this.productRepository = productRepository;
        this.authorizationUtils = authorizationUtils;
    }


    @Override
    public ReviewDTO saveReview(ReviewDTO reviewDTO, String token) {
        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("email", username));

        // Map DTO to entity
        Review review = modelMapper.map(reviewDTO, Review.class);

        // Fetch and set product
        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("product_id", reviewDTO.getProductId().toString()));
        review.setProduct(product);

        // Set the user obtained from token
        review.setUser(user);

        // Check for existing review by the user for the same product
        if (reviewRepository.existsByProductAndUser(product, user)) {
            throw new AccessDeniedException("You have already reviewed this product");
        }

        // Set images if provided
        List<ReviewImage> reviewImages = mapImageDtosToReviewImages(reviewDTO.getImageDtos(), review);
        review.setReviewImages(reviewImages);

        // Save the review
        Review savedReview = reviewRepository.save(review);

        // Update the product's total rating
        productService.updateProductTotalRating(product.getId());

        // Map saved review back to DTO
        ReviewDTO savedReviewDTO = modelMapper.map(savedReview, ReviewDTO.class);

        // Set image DTOs in response
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
                .orElseThrow(() -> new UserNotFoundException("email", username));

        // Authorization check
        authorizationUtils.checkUserOrAdminRole(token, user.getId());

        if (!existingReview.getUser().equals(user) && user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to update this review");
        }

        // Update fields
        existingReview.setRating(reviewDTO.getRating());
        existingReview.setComment(reviewDTO.getComment());

        // Update images
        List<ReviewImage> existingImages = existingReview.getReviewImages();
        List<ReviewImage> updatedImages = mapImageDtosToReviewImages(reviewDTO.getImageDtos(), existingReview);
        existingImages.removeIf(image -> !updatedImages.contains(image));
        updatedImages.stream().filter(image -> !existingImages.contains(image)).forEach(existingImages::add);

        existingReview.setReviewImages(existingImages);

        // Save updated review
        Review updatedReview = reviewRepository.save(existingReview);

        // Update product's total rating
        productService.updateProductTotalRating(updatedReview.getProduct().getId());

        // Convert updated review to DTO
        ReviewDTO updatedReviewDTO = modelMapper.map(updatedReview, ReviewDTO.class);
        List<ReviewImageDto> imageDtos = updatedReview.getReviewImages().stream()
                .map(image -> modelMapper.map(image, ReviewImageDto.class))
                .collect(Collectors.toList());
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

        authorizationUtils.checkUserOrAdminRole(token, user.getId());

        reviewRepository.deleteById(id);

        productService.updateProductTotalRating(review.getProduct().getId());
    }

    @Override
    public ReviewDTO likeReview(Long reviewId, String token) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("id", reviewId.toString()));

        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));
        authorizationUtils.checkUserOrAdminRole(token, user.getId());
        userReviewLikeRepository.findByUserIdAndReviewId(user.getId(), reviewId)
                .ifPresent(like -> {
                    throw new IllegalStateException("User has already liked this review");
                });

        UserReviewLike like = new UserReviewLike();
        like.setUser(user);
        like.setReview(review);
        userReviewLikeRepository.save(like);

        review.setLikeCount(review.getLikeCount() + 1);
        Review updatedReview = reviewRepository.save(review);

        return modelMapper.map(updatedReview, ReviewDTO.class);
    }

    @Override
    @Transactional
    public void deleteReviewsByUserIdAndProductId(Long userId, Long productId, String token) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("user_id",userId.toString()));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException("product_id",productId.toString()));
        authorizationUtils.checkUserOrAdminRole(token, user.getId());
        reviewRepository.deleteReviewByProductAndUser(product, user);
        productService.updateProductTotalRating(productId);
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
