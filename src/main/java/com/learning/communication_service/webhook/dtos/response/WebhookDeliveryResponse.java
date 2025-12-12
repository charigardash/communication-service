package com.learning.communication_service.webhook.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class WebhookDeliveryResponse {

    private String id;
    private String webhookId;
    private String webhookName;
    private String eventId;
    private WebhookEventType eventType;
    private String targetUrl;
    private String status;
    private int attemptCount;
    private Integer responseCode;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextRetryAt;

    private long processingTimeMs;

}
