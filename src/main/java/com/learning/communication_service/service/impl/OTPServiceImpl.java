package com.learning.communication_service.service.impl;

import com.common.base.ratelimit.exception.RateLimitExceededException;
import com.common.base.ratelimit.service.RateLimitingService;
import com.common.base.ratelimit.service.SecurityService;
import com.learning.communication_service.dbEntity.OTPVerification;
import com.learning.communication_service.enums.OTPType;
import com.learning.communication_service.factory.CommunicationChannelService;
import com.learning.communication_service.repository.OTPVerificationRepository;
import com.learning.communication_service.service.OTPService;
import com.learning.communication_service.service.OTPWebhookIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;


@Service
@Slf4j
public class OTPServiceImpl implements OTPService {

    @Autowired
    private OTPVerificationRepository otpVerificationRepository;

    @Autowired
    private SecurityService securityService;//Sliding window

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private OTPWebhookIntegrationService webhookIntegrationService;

    private final Map<String, CommunicationChannelService> factory;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    public OTPServiceImpl(Map<String, CommunicationChannelService> factory) {
        this.factory = factory;
    }

    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for(int i=0;i<OTP_LENGTH;i++){
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Override
    public void sendOTP(String request, OTPType type, String clientIp) {
        String rateLimitType = type == OTPType.EMAIL ? RateLimitingService.RATE_LIMIT_OTP_EMAIL : RateLimitingService.RATE_LIMIT_OTP_SMS;
        RateLimitingService.RateLimitResult result = rateLimitingService.checkCompositeRateLimit(request, rateLimitType);
        if(result.isRateLimited()){
            webhookIntegrationService.triggerRateLimitExceedEvent(rateLimitType, clientIp, "/communication/auth/otp/end", 5);
            throw new RateLimitExceededException("Too many OTP requests. Please try again later.");
        }
        try {
            CommunicationChannelService channelService = factory.get(type.toString());
            if (channelService == null) {
                throw new RuntimeException("Channel doesn't exist");
            }
            otpVerificationRepository.invalidatePreviousOtps(request, type);
            String otp = generateOTP();
            OTPVerification otpVerification = new OTPVerification(request, otp, type, OTP_EXPIRY_MINUTES);
            otpVerificationRepository.save(otpVerification);
            // Send OTP
            channelService.sendOTP(request, otp);
            log.info("OTP sent successfully to: {}. Remaining requests: {}",
                    request, result.getRemainingRequests());
            webhookIntegrationService.triggerOtpSentEvent(rateLimitType, type, otp, maskedOtp(otp));
//            securityService.recordAttempt(request, rateLimitType, true);
        }catch (Exception e){
            log.error("Failed to send OTP to: {}", request, e);
//            securityService.recordAttempt(request, rateLimitType, false);
            throw e;
        }
    }

    private String maskedOtp(String otp) {
        if(otp == null || otp.length() < 4) return "****";
        return otp.substring(0, 2)+"***"+otp.substring(otp.length()-1);
    }

    @Override
    public boolean verifyOTP(String identifier, String otp, OTPType type) {
        Optional<OTPVerification> otpVerification = otpVerificationRepository.findByIdentifierAndOtpAndTypeAndUsedFalse(identifier, otp, type);

        boolean isValid = false;
        String reason = "Invalid OTP";
        if(otpVerification.isPresent()){
            OTPVerification verification = otpVerification.get();
            if(verification.isExpired()) {
                reason = "Otp expired";
            }
            // Mark OTP as used
            verification.setUsed(true);
            verification.setVerifiedAt(Instant.now());
            otpVerificationRepository.save(verification);
            isValid = true;
            reason = "OTP verified successfully";
        }
        webhookIntegrationService.triggerOtpVerifiedEvent(identifier, type, isValid, reason);
        return isValid;
    }

    @Override
    public void cleanupExpiredOtps() {
        List<OTPVerification> allOtps = otpVerificationRepository.findAll();
        List<OTPVerification> expiredOtps = allOtps.stream().filter(otp -> otp.getExpiresAt().isBefore(Instant.now()) && !otp.isUsed())
                .toList();
        expiredOtps.forEach(otp -> otp.setUsed(true));
    }

}
