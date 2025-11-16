package com.learning.communication_service.repository;

import com.learning.communication_service.dbEntity.OTPVerification;
import com.learning.communication_service.enums.OTPType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OTPVerificationRepository extends MongoRepository<OTPVerification, String>, OTPVerificationCustomRepository {

    // Find a valid OTP entry
    Optional<OTPVerification> findByIdentifierAndOtpAndTypeAndUsedFalse(String identifier, String otp, OTPType type);

    // Find all active OTPs for an identifier to them
    List<OTPVerification> findByIdentifierAndTypeAndUsedFalse(String identifier, OTPType type);

    // Custom query to invalidate previous OTPs
    @Query("{'identifier': ?0, 'type': ?1, 'used' : false, 'expiresAt' : {$gt: ?2}}")
    List<OTPVerification> findActiveOtp(String identifier, OTPType type, Instant currentTime);

}
