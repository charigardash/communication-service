package com.learning.communication_service.webhook.controller;

import com.learning.communication_service.webhook.dtos.request.WebhookRequest;
import com.learning.communication_service.webhook.dtos.response.ErrorDetails;
import com.learning.communication_service.webhook.exception.WebhookException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebhookException.class)
    public ResponseEntity<?> handleCustomException(WebhookException e, WebhookRequest request){
        ErrorDetails<WebhookException> errorDetails = new ErrorDetails<>(LocalDateTime.now(), e);
        return new ResponseEntity<>(errorDetails, e.getStatus());
    }
}
