package com.learning.communication_service.webhook.exception;

import org.springframework.http.HttpStatus;

public class WebhookException extends RuntimeException{

    private final HttpStatus status;

    public WebhookException(String message, HttpStatus status){
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
