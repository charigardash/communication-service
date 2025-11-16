package com.learning.communication_service.service;


import com.learning.communication_service.enums.OTPType;
import org.springframework.stereotype.Service;

@Service
public interface OTPService {
    void sendOTP(String request, OTPType type);

    boolean verifyOTP(String identifier, String otp, OTPType type);

    void cleanupExpiredOtps();
}
