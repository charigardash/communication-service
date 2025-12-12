package com.learning.communication_service.webhook.service.impl;

import com.learning.communication_service.dbEntity.webhook.Webhook;
import com.learning.communication_service.webhook.dtos.request.WebhookRequest;
import com.learning.communication_service.webhook.dtos.response.PaginatedResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookResponse;
import com.learning.communication_service.webhook.exception.WebhookException;
import com.learning.communication_service.webhook.repository.WebhookRepository;
import com.learning.communication_service.webhook.service.WebhookManagementService;
import com.learning.communication_service.webhook.util.WebhookUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.learning.communication_service.webhook.util.ValidationUtil.validateWebhookRequest;
import static com.learning.communication_service.webhook.util.WebhookUtil.convertToResponse;
import static com.learning.communication_service.webhook.util.WebhookUtil.getWebhookFromRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookManagementServiceImpl implements WebhookManagementService {

    private final WebhookRepository webhookRepository;

    @Transactional
    @Override
    public WebhookResponse createWebhook(WebhookRequest webhookRequest) {
        validateWebhookRequest(webhookRequest);
        if(webhookRepository.existsByTargetUrlAndActiveTrue(webhookRequest.getTargetUrl())){
            throw new WebhookException("Active webhook already exists for URL: " +webhookRequest.getTargetUrl(), HttpStatus.CONFLICT);
        }

        Webhook webhook = getWebhookFromRequest(webhookRequest);

        Webhook savedWebhook = webhookRepository.save(webhook);

        log.info("Created webhook: {} -> {}", savedWebhook.getName(), savedWebhook.getTargetUrl());

        return convertToResponse(savedWebhook);
    }

    @Transactional(readOnly = true)
    @Override
    public WebhookResponse getWebhook(String id) {
        Webhook webhook =  webhookRepository.findById(id).orElseThrow(() ->new WebhookException("Webhook not found: " + id,
                HttpStatus.NOT_FOUND));

        return convertToResponse(webhook);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<WebhookResponse> getAllWebhooks(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
        Page<Webhook> webhooks = webhookRepository.findAll(pageable);
        return WebhookUtil.createPaginatedResponse(webhooks);
    }
}
