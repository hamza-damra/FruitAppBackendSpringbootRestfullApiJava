package com.hamza.fruitsappbackend.modules.user.repository;

import com.hamza.fruitsappbackend.modules.user.entity.AccountVerification;
import com.hamza.fruitsappbackend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface AccountVerificationRepository extends JpaRepository<AccountVerification, Integer> {

    Optional<AccountVerification> findByUser(User user);

    Optional<AccountVerification> findByOtpAndExpirationTimeAfter(Integer otp, Date date);

    void deleteByExpirationTimeBefore(Date now);
}
