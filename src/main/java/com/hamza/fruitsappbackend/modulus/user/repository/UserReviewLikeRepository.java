package com.hamza.fruitsappbackend.modulus.user.repository;

import com.hamza.fruitsappbackend.modulus.user.entity.UserReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserReviewLikeRepository extends JpaRepository<UserReviewLike, Long> {
    Optional<UserReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);
}
