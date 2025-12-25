package com.learning.communication_service.webhook.exception;

public class WebhookSecurityException extends RuntimeException{

    public WebhookSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
