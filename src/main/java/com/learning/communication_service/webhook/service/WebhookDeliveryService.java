package com.learning.communication_service.webhook.service;

import com.learning.communication_service.dbEntity.webhook.Webhook;
import com.learning.communication_service.dbEntity.webhook.WebhookDelivery;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
public interface WebhookDeliveryService {
    @Async
    @Transactional
    CompletableFuture<Void> deliverWebhook(String eventId, WebhookEventType eventType, Object payload);

    CompletableFuture<WebhookDelivery> deliverToSingleWebhook(Webhook webhook, String eventId, WebhookEventType webhookEventType, String payloadJson);
}
