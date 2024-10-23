package com.hamza.fruitsappbackend.modules.user.repository;

import com.hamza.fruitsappbackend.modules.user.entity.ForgotPassword;
import com.hamza.fruitsappbackend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {

    Optional<ForgotPassword> findByUser(User user);

    Optional<ForgotPassword> findByUserAndOtpAndExpirationTimeAfter(User user, Integer otp, Date date);

    // Add this method to find by OTP and check expiration
    Optional<ForgotPassword> findByOtpAndExpirationTimeAfter(Integer otp, Date date);
}
