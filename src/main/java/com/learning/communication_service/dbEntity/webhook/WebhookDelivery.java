package com.learning.communication_service.dbEntity.webhook;


import com.learning.communication_service.webhook.enums.WebhookEventType;
import com.learning.communication_service.webhook.enums.WebhookStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "webhook_deliveries")
public class WebhookDelivery {

    @Id
    private String id;

    @Indexed
    private String webhookId;

    @Indexed
    private String eventId;

    private WebhookEventType eventType;

    @Field("payload")
    private String payload;

    @Field("target_url")
    private String targetUrl;

    private WebhookStatus status = WebhookStatus.PENDING;

    private int attemptCount = 0;

    private int maxAttempt;

    @Field("response_code")
    private Integer responseCode;

    @Field("response_body")
    private String responseBody;

    @Field("error_message")
    private String errorMessage;

    private Map<String, String> requestHeaders;

    private Map<String, String> responseHeaders;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    private LocalDateTime nextRetryAt;

    private long processingTimeMs;

    // Constructors
    public WebhookDelivery() {
        this.createdAt = LocalDateTime.now();
    }

    public WebhookDelivery(String webhookId, String eventId, WebhookEventType eventType,
                           String payload, String targetUrl) {
        this();
        this.webhookId = webhookId;
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.targetUrl = targetUrl;
    }

    // Helper methods
    public boolean canRetry(){
        return this.attemptCount < this.maxAttempt &&
                this.status != WebhookStatus.SENT;
    }

    public void markAsSent(int responseCode, String responseBody){
        this.status = WebhookStatus.SENT;
        this.responseCode = responseCode;
        this.responseBody = responseBody;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = WebhookStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markAsRetrying(LocalDateTime nextRetryAt){
        this.status = WebhookStatus.RETRYING;
        this.nextRetryAt = nextRetryAt;
    }
}
