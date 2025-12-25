package com.learning.communication_service.webhook.service;

import com.learning.communication_service.webhook.dtos.request.WebhookRequest;
import com.learning.communication_service.webhook.dtos.request.WebhookUpdateRequest;
import com.learning.communication_service.webhook.dtos.response.PaginatedResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookDeliveryResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookStatistics;
import com.learning.communication_service.webhook.enums.WebhookStatus;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public interface WebhookManagementService {
    WebhookResponse createWebhook(@Valid WebhookRequest webhookRequest);

    WebhookResponse getWebhook(String id);

    PaginatedResponse<WebhookResponse> getAllWebhooks(int page, int size);

    PaginatedResponse<WebhookResponse> getWebhookByUser(String userId, int page, int size);

    WebhookResponse updateWebhook(String webhookId, @Valid WebhookUpdateRequest webhookUpdateRequest);

    void deleteWebhook(String id);

    void disableWebhook(String id);

    void enableWebhook(String id);

    void triggerTestWebhook(String id, Object testPayload);

    PaginatedResponse<WebhookDeliveryResponse> getWebhookDeliveries(String id, WebhookStatus webhookStatus, int pageNumber, int pageSize);

    void retryFailedDeliveries(String id);

    WebhookStatistics getStatistics(String id);
}
