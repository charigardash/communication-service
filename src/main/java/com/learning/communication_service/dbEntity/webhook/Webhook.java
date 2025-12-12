package com.learning.communication_service.dbEntity.webhook;

import com.learning.communication_service.webhook.enums.RetryStrategy;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Document(collection = "webhooks")
public class Webhook {

    @Setter
    @Id
    private String id;

    @Indexed
    private String name;

    @Indexed
    private String userId;

    @Field("url")
    private String targetUrl;

    @Field("events")
    private Set<WebhookEventType> subscribedEvents = new HashSet<>();

    @Field("secret")
    private String signingSecret;

    @Field("headers")
    private Map<String, String> customHeaders = new HashMap<>();

    private boolean active = true;

    private int timeoutSeconds = 30;

    private int retryCount = 3;

    private RetryStrategy retryStrategy = RetryStrategy.EXPONENTIAL_BACKOFF;

    @Setter
    @Field("created_at")
    private LocalDateTime createdAt;

    @Setter
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Setter
    private LocalDateTime lastTriggeredAt;

    @Setter
    private WebhookStatistics statistics = new WebhookStatistics();

    public Webhook(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Webhook(String name, String targetUrl){
        this();
        this.name = name;
        this.targetUrl = targetUrl;
    }

    public void setName(String name){
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public void setUserId(String userId) {
        this.userId = userId;
        this.updatedAt = LocalDateTime.now();
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSubscribedEvents(Set<WebhookEventType> subscribedEvents) {
        this.subscribedEvents = subscribedEvents;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSigningSecret(String signingSecret) {
        this.signingSecret = signingSecret;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
        this.updatedAt = LocalDateTime.now();
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.updatedAt = LocalDateTime.now();
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        this.updatedAt = LocalDateTime.now();
    }

    public void setRetryStrategy(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
        this.updatedAt = LocalDateTime.now();
    }


    // Helper methods
    public void addEvent(WebhookEventType event){
        this.subscribedEvents.add(event);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeEvent(WebhookEventType event){
        this.subscribedEvents.remove(event);
        this.updatedAt = LocalDateTime.now();
    }

    public void addCustomHeader(String key, String value){
        this.customHeaders.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementSuccessCount() {
        this.statistics.incrementSuccessCount();
        this.lastTriggeredAt = LocalDateTime.now();
    }

    public void incrementFailureCount() {
        this.statistics.incrementFailureCount();
        this.lastTriggeredAt = LocalDateTime.now();
    }


    @Document
    @Data
    public class WebhookStatistics {
        private long totalAttempts = 0;
        private long successfulDeliveries = 0;
        private long failedDeliveries = 0;
        private long lastDeliveryAttempt;

        public void incrementSuccessCount() {
            this.totalAttempts++;
            this.successfulDeliveries++;
            lastDeliveryAttempt = System.currentTimeMillis();
        }

        public void incrementFailureCount() {
            this.totalAttempts++;
            this.failedDeliveries++;
            lastDeliveryAttempt = System.currentTimeMillis();
        }

        public double getSuccessRate() {
            return totalAttempts > 0 ? (successfulDeliveries * 100.0) / totalAttempts : 0;
        }
    }

}
