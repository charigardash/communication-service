package com.learning.communication_service.service;

import com.learning.communication_service.enums.OTPType;
import org.springframework.scheduling.annotation.Async;

public interface OTPWebhookIntegrationService {

    @Async
    void triggerOtpSentEvent(String identifier, OTPType otpType, String otp, String maskedOtp);

    @Async
    void triggerOtpVerifiedEvent(String identifier, OTPType otpType, boolean success, String reason);

    @Async
    void triggerRateLimitExceedEvent(String identifier, String clientIp, String endpoint, int attempts);

    @Async
    void triggerSuspiciousActivityEvent(String identifier, String activity, String details);
}
