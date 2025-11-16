package com.learning.communication_service.scheduler;

import com.learning.communication_service.service.OTPService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OTPScheduler {

    private final OTPService otpService;

    public OTPScheduler(OTPService otpService) {
        this.otpService = otpService;
    }

    // Clean up expired OTPs every hour
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredOtps() {
        otpService.cleanupExpiredOtps();
    }
}
