package com.learning.communication_service.dbEntity;

import com.learning.communication_service.enums.OTPType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "otp_verifications")
@Data
@CompoundIndex(name = "identifier_type_used_index",
        def = "{'identifier': 1, 'type': 1, 'used': 1}")
public class OTPVerification {

    @Id // MongoDB uses String IDs by default, but ObjectId is common
    private String id;

    @Indexed
    private String identifier;  //email or phone number

    private String otp;

    @Indexed
    private OTPType type;

    private Instant createdAt;

    private Instant expiresAt;

    private Instant verifiedAt;

    @Indexed
    private boolean used = false;

    public OTPVerification(String identifier, String otp, OTPType type, long expiryMinutes) {
        this.identifier = identifier;
        this.otp = otp;
        this.type = type;
        this.createdAt = Instant.now();
        this.expiresAt = Instant.now().plusMillis(expiryMinutes *60*1000);
    }

    public boolean isExpired(){
        return Instant.now().isAfter(expiresAt);
    }

}
