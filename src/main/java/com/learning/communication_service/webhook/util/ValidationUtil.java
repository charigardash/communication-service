package com.learning.communication_service.webhook.util;

import com.learning.communication_service.webhook.dtos.request.WebhookRequest;
import com.learning.communication_service.webhook.exception.WebhookException;
import org.springframework.http.HttpStatus;

public class ValidationUtil {

    public static void validateWebhookRequest(WebhookRequest request){
        if(request.getSubscribedEvents().isEmpty()){
            throw new WebhookException("At least one event must be subscribed", HttpStatus.BAD_REQUEST);
        }
        if(!request.getTargetUrl().startsWith("https://") && request.getTargetUrl().startsWith("http://")){
            throw new WebhookException("URL must start with http:// or https://",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
