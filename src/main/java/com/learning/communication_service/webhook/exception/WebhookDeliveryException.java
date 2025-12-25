package com.learning.communication_service.webhook.exception;

import org.springframework.http.HttpStatus;

public class WebhookDeliveryException extends RuntimeException{

    public WebhookDeliveryException(String message){
        super(message);
    }

    public WebhookDeliveryException(String message, Throwable t){
        super(message, t);
    }
}
