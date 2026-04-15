package com.userService.UserService.scheduler;

import com.userService.UserService.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupScheduler(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // 🔥 Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteExpiredTokens() {

        System.out.println("Running scheduled cleanup for expired tokens...");

        refreshTokenRepository.deleteAllExpired(LocalDateTime.now());

        System.out.println("Expired tokens deleted successfully");
    }
}
