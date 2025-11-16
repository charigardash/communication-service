package com.learning.communication_service.repository;

import com.learning.communication_service.enums.OTPType;
import org.springframework.stereotype.Repository;

@Repository
public interface OTPVerificationCustomRepository {
    void invalidatePreviousOtps(String identifier, OTPType type);
}
