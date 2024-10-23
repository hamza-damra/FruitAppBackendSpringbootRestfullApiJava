package com.hamza.fruitsappbackend.configuration;

import com.hamza.fruitsappbackend.modules.user.repository.AccountVerificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AccountVerificationCleanupTask {

    private final AccountVerificationRepository accountVerificationRepository;

    @Autowired
    public AccountVerificationCleanupTask(AccountVerificationRepository accountVerificationRepository) {
        this.accountVerificationRepository = accountVerificationRepository;
    }

    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void deleteExpiredVerifications() {
        Date now = new Date();
        accountVerificationRepository.deleteByExpirationTimeBefore(now);
    }
}
