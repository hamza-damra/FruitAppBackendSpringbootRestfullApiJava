package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.entity.Review;
import com.hamza.fruitsappbackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private Review review;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user.setPasswordHash("hashedpassword");
        userRepository.save(user);

        Product product = new Product();
        product.setName("Apple");
        product.setDescription("Fresh Red Apple");
        product.setPrice(1.5);
        product.setStockQuantity(100);
        productRepository.save(product);

        review = new Review();
        review.setRating(5);
        review.setComment("Great product!");
        review.setUser(user);
        review.setProduct(product);
    }

    @Test
    void testSaveReview() {
        Review savedReview = reviewRepository.save(review);
        assertNotNull(savedReview.getId());
    }

    @Test
    void testFindReviewById() {
        Review savedReview = reviewRepository.save(review);
        Optional<Review> retrievedReview = reviewRepository.findById(savedReview.getId());
        assertTrue(retrievedReview.isPresent());
        assertEquals(savedReview.getId(), retrievedReview.get().getId());
    }

    @Test
    void testUpdateReview() {
        Review savedReview = reviewRepository.save(review);
        savedReview.setRating(4);
        Review updatedReview = reviewRepository.save(savedReview);
        assertEquals(4, updatedReview.getRating());
    }

    @Test
    void testDeleteReview() {
        Review savedReview = reviewRepository.save(review);
        reviewRepository.deleteById(savedReview.getId());
        Optional<Review> deletedReview = reviewRepository.findById(savedReview.getId());
        assertFalse(deletedReview.isPresent());
    }
}
