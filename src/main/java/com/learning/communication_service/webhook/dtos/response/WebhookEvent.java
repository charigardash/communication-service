package com.learning.communication_service.webhook.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class WebhookEvent<T>  { //TODO: T

    private String eventId;

    private WebhookEventType eventType;

    private T data;

    private Map<String, Object> metadata;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String source = "Communication-Service";

    private String version = "1.0";

    public WebhookEvent(WebhookEventType type, T data){
        this.eventId = UUID.randomUUID().toString();
        this.eventType = type;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }
}
