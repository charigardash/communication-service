package com.learning.communication_service.requestEntity;


import com.learning.communication_service.enums.OTPType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class OTPVerificationRequest implements Serializable {

    @NotBlank(message = "Identifier is required")
    private String identifier;

    @NotBlank
    @Size(min = 6, max = 6, message = "Otp must be 6 digits")
    private String otp;

    @NotNull(message = "OTP type is required")
    private OTPType type;
}
