package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.AccountVerification;
import com.hamza.fruitsappbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface AccountVerificationRepository extends JpaRepository<AccountVerification, Integer> {

    Optional<AccountVerification> findByUser(User user);

    Optional<AccountVerification> findByOtpAndExpirationTimeAfter(Integer otp, Date date);
}
