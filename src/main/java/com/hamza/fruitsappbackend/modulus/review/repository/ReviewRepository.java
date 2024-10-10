package com.hamza.fruitsappbackend.modulus.review.repository;
import com.hamza.fruitsappbackend.modulus.product.entity.Product;
import com.hamza.fruitsappbackend.modulus.review.entity.Review;
import com.hamza.fruitsappbackend.modulus.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    List<Review> findByUserId(Long userId);
    void deleteReviewByProductAndUser(Product product, User user);
    boolean existsByProductAndUser(Product product, User user);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    void deleteAllByProductId(Long id);
}