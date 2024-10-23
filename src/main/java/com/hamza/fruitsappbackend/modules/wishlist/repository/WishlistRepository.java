package com.hamza.fruitsappbackend.modules.wishlist.repository;

import com.hamza.fruitsappbackend.modules.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserId(Long userId);
    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

    void deleteAllByProductId(Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long id);

    void deleteAllByUserId(Long userId);
}
