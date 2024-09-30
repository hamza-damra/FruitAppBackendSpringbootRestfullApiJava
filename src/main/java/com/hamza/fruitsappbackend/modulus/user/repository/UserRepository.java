package com.hamza.fruitsappbackend.modulus.user.repository;

import com.hamza.fruitsappbackend.modulus.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.accountVerification.otp = :otp")
    Optional<User> findByOtp(Integer otp);

    User getUserById(Long userId);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.wishlistItems w " +
            "LEFT JOIN FETCH u.cart c " +
            "WHERE u.email = :email")
    Optional<User> findUserWithWishlistAndCartByEmail(@Param("email") String email);

}
