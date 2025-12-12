package com.learning.communication_service.webhook.service;

import com.learning.communication_service.webhook.dtos.request.WebhookRequest;
import com.learning.communication_service.webhook.dtos.response.PaginatedResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface WebhookManagementService {
    WebhookResponse createWebhook(@Valid WebhookRequest webhookRequest);

    WebhookResponse getWebhook(String id);

    @Transactional(readOnly = true)
    PaginatedResponse<WebhookResponse> getAllWebhooks(int page, int size);
}
