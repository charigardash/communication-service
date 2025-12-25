package com.learning.communication_service.webhook.util;

import com.learning.communication_service.dbEntity.webhook.Webhook;
import com.learning.communication_service.dbEntity.webhook.WebhookDelivery;
import com.learning.communication_service.webhook.dtos.request.WebhookRequest;
import com.learning.communication_service.webhook.dtos.response.PaginatedResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookDeliveryResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WebhookUtil {
    public static String generateRandomSecret() {
        return UUID.randomUUID().toString().replace("-","");
    }

    public static Webhook getWebhookFromRequest(WebhookRequest webhookRequest) {
        Webhook webhook = new Webhook();
        webhook.setName(webhookRequest.getName());
        webhook.setTargetUrl(webhookRequest.getTargetUrl());
        webhook.setSubscribedEvents(webhookRequest.getSubscribedEvents());
        webhook.setSigningSecret(webhookRequest.getSigningSecret() != null?
                webhookRequest.getSigningSecret() : generateRandomSecret());
        webhook.setCustomHeaders(webhookRequest.getCustomHeaders());
        webhook.setActive(webhookRequest.isActive());
        webhook.setTimeoutSeconds(webhookRequest.getTimeoutSeconds());
        webhook.setRetryCount(webhookRequest.getRetryCount());
        webhook.setRetryStrategy(webhookRequest.getRetryStrategy());
        webhook.setUserId(webhookRequest.getUserId());

        return webhook;
    }

    public static WebhookResponse convertToResponse(Webhook webhook){

        WebhookResponse response = new WebhookResponse();
        response.setId(webhook.getId());
        response.setName(webhook.getName());
        response.setUserId(webhook.getUserId());
        response.setTargetUrl(webhook.getTargetUrl());
        response.setSubscribedEvents(webhook.getSubscribedEvents());
        response.setActive(webhook.isActive());
        response.setTimeoutSeconds(webhook.getTimeoutSeconds());
        response.setRetryCount(webhook.getRetryCount());
        response.setRetryStrategy(webhook.getRetryStrategy());
        response.setCreatedAt(webhook.getCreatedAt());
        response.setUpdatedAt(webhook.getUpdatedAt());
        response.setLastTriggeredAt(webhook.getLastTriggeredAt());

        WebhookResponse.Statistics stats = new WebhookResponse.Statistics();

        Webhook.WebhookStatistics webhookStats = webhook.getStatistics();
        stats.setTotalAttempts(webhookStats.getTotalAttempts());
        stats.setSuccessfulDeliveries(webhookStats.getSuccessfulDeliveries());
        stats.setFailedDeliveries(webhookStats.getFailedDeliveries());
        stats.setSuccessRate(webhookStats.getSuccessRate());
        response.setStatistics(stats);

        return response;
    }

    public static PaginatedResponse<WebhookResponse> createPaginatedResponse(Page<Webhook> page) {
        List<WebhookResponse> content = page.getContent().stream()
                .map(WebhookUtil::convertToResponse)
                .toList();
        return createPaginatedResponse(page, content);
    }

    public static <T> PaginatedResponse<T> createPaginatedResponse(Page<?> page, List<T> content) {
        PaginatedResponse<T> response = new PaginatedResponse<>();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }

    public static WebhookDeliveryResponse convertToDeliveryResponse(WebhookDelivery delivery){
        return WebhookDeliveryResponse.builder()
                .id(delivery.getId())
                .webhookId(delivery.getWebhookId())
                .eventId(delivery.getEventId())
                .targetUrl(delivery.getTargetUrl())
                .status(delivery.getStatus().toString())
                .attemptCount(delivery.getAttemptCount())
                .responseCode(delivery.getResponseCode())
                .errorMessage(delivery.getErrorMessage())
                .createdAt(delivery.getCreatedAt())
                .sentAt(delivery.getSentAt())
                .nextRetryAt(delivery.getNextRetryAt())
                .processingTimeMs(delivery.getProcessingTimeMs())
                .build();
    }
}
