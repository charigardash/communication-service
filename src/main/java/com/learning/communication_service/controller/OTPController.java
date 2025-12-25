package com.learning.communication_service.controller;

import com.learning.communication_service.enums.OTPType;
import com.learning.communication_service.requestEntity.OTPRequest;
import com.learning.communication_service.requestEntity.OTPVerificationRequest;
import com.learning.communication_service.responseEntity.OTPResponse;
import com.learning.communication_service.service.OTPService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/communication/auth/otp")
public class OTPController {

    private final OTPService otpService;

    public OTPController(OTPService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendOTP(@Valid @RequestBody OTPRequest request, HttpServletRequest httpServletRequest){
        if(StringUtils.isNotEmpty(request.getEmail())){
            otpService.sendOTP(request.getEmail(), OTPType.EMAIL, httpServletRequest.getRemoteAddr());
            return ResponseEntity.ok(new OTPResponse(true, "OTP sent to email successfully"));
        }else if(StringUtils.isNotEmpty(request.getPhoneNumber())){
            otpService.sendOTP(request.getPhoneNumber(), OTPType.SMS, httpServletRequest.getRemoteAddr());
            return ResponseEntity.ok(new OTPResponse(true, "OTP sent to phone successfully"));
        }
        return ResponseEntity.badRequest().body(new OTPResponse(false, "Either email or phone number must be provided"));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody OTPVerificationRequest request){
        boolean isValid = otpService.verifyOTP(request.getIdentifier(), request.getOtp(),request.getType());
        if (isValid) {
            return ResponseEntity.ok(new OTPResponse(true, "OTP verified successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(new OTPResponse(false, "Invalid or expired OTP"));
        }
    }
}
