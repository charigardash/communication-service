package com.learning.communication_service.controller;

import com.common.base.ratelimit.exception.RateLimitExceededException;
import com.learning.communication_service.responseEntity.OTPResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericResponse(Exception e){
        Map<String, Object> response = new HashMap<>();
        response.put("error", "internal server error");
        response.put("message", e.getMessage());
        response.put("status", 500);
        response.put("timestamp", Instant.now());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<OTPResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        OTPResponse response = new OTPResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}
