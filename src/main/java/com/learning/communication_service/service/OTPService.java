package com.learning.communication_service.service;


import com.learning.communication_service.enums.OTPType;

public interface OTPService {
    void sendOTP(String request, OTPType type, String remoteAddr);

    boolean verifyOTP(String identifier, String otp, OTPType type);

    void cleanupExpiredOtps();
}
