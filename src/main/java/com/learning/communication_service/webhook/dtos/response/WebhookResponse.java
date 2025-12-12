package com.learning.communication_service.webhook.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.learning.communication_service.webhook.enums.RetryStrategy;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
public class WebhookResponse {

    private String id;
    private String name;
    private String userId;
    private String targetUrl;
    private Set<WebhookEventType> subscribedEvents;
    private boolean active;
    private int timeoutSeconds;
    private int retryCount;
    private RetryStrategy retryStrategy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTriggeredAt;

    private Statistics statistics;

    @Data
    @NoArgsConstructor
    public static class Statistics {
        private long totalAttempts;
        private long successfulDeliveries;
        private long failedDeliveries;
        private double successRate;
    }
}
