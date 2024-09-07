package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by their email address
    Optional<User> findByEmail(String email);

    // Update a user's password based on their email
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.password = :newPassword WHERE u.email = :email")
    void updatePassword(String email, String newPassword);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.accountVerification.otp = :otp")
    Optional<User> findByOtp(Integer otp);
}
