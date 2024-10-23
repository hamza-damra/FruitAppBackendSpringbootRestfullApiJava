package com.hamza.fruitsappbackend.modules.user.repository;

import com.hamza.fruitsappbackend.modules.user.entity.ResetEmail;
import com.hamza.fruitsappbackend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface ResetEmailRepository extends JpaRepository<ResetEmail, Integer> {

    Optional<ResetEmail> findByUser(User user);

    Optional<ResetEmail> findByUserAndOtpAndExpirationTimeAfter(User user, Integer otp, Date date);

    Optional<ResetEmail> findByOtpAndExpirationTimeAfter(Integer otp, Date date);

}
